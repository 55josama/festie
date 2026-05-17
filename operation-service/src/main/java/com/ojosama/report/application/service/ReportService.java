package com.ojosama.report.application.service;

import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.OutboxEventPublisher;
import com.ojosama.common.response.PageResponse;
import com.ojosama.report.application.dto.command.CreateReportCommand;
import com.ojosama.report.application.dto.command.UpdateReportCommand;
import com.ojosama.report.application.dto.query.ListReportQuery;
import com.ojosama.report.application.dto.result.ReportInfoResult;
import com.ojosama.report.application.dto.result.ReportResult;
import com.ojosama.report.domain.event.payload.BlacklistReviewRequestedEvent;
import com.ojosama.report.domain.event.payload.TargetBlindedEvent;
import com.ojosama.report.domain.event.payload.TargetUnblindedEvent;
import com.ojosama.report.domain.exception.ReportErrorCode;
import com.ojosama.report.domain.exception.ReportException;
import com.ojosama.report.domain.model.entity.Report;
import com.ojosama.report.domain.model.enums.ReportStatus;
import com.ojosama.report.domain.model.enums.ReporterType;
import com.ojosama.report.domain.repository.ReportRepository;
import com.ojosama.report.infrastructure.client.ChatClient;
import com.ojosama.report.infrastructure.lock.RedissonDistributedLock;
import com.ojosama.report.infrastructure.lock.config.DistributedLockProperties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final OutboxEventPublisher outbox;
    private final ChatClient chatClient;
    private final RedissonDistributedLock redissonDistributedLock;
    private final DistributedLockProperties lockProperties;

    @Value("${spring.kafka.topic.report-blinded}")
    private String targetBlindedTopic;

    @Value("${spring.kafka.topic.report-unblinded}")
    private String targetUnblindedTopic;

    @Value("${spring.kafka.topic.blacklist-requested}")
    private String blacklistReviewRequestedTopic;

    // 신고 생성
    @Transactional
    @CacheEvict(cacheNames = {"report", "reportList"}, allEntries = true)
    public ReportInfoResult createReport(CreateReportCommand command, ReporterType reporterType) {
        boolean lockAcquired = acquireDistributedLock(command.targetId());

        try {
            validateDuplicateReport(command.reporterId(), command.targetId());

            Report savedReport = saveReportSafely(command.toEntity(reporterType));

            checkAndProcessAutomaticBlind(command, savedReport);

            return ReportInfoResult.from(savedReport);

        } finally {
            releaseDistributedLock(command.targetId(), lockAcquired);
        }
    }

    // 신고 목록 조회
    @Cacheable(
            cacheNames = "reportList",
            key = "(#listReportQuery.status() != null ? #listReportQuery.status().name() : 'ALL') + "
                    + "':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize + ':sort:' + #pageable.sort.toString()"
    )
    public PageResponse<ReportResult> getReportList(ListReportQuery listReportQuery, Pageable pageable) {
        Page<Report> reports = fetchReportsByQuery(listReportQuery, pageable);

        return PageResponse.from(reports.map(ReportResult::from));
    }

    // 신고 상세 조회
    @Cacheable(cacheNames = "report", key = "#reportId")
    public ReportInfoResult getReportInfo(UUID reportId) {
        Report report = findReportById(reportId);
        return ReportInfoResult.from(report);
    }

    // 신고 상태 변경 및 운영진 메모 추가
    @Transactional
    @CacheEvict(cacheNames = {"report", "reportList"}, allEntries = true)
    public ReportInfoResult updateReport(UUID reportId, UpdateReportCommand command) {
        Report report = findReportById(reportId);
        validateReportIsAutoBlinded(report);
        validateStatusIsValid(command.status());

        if (command.status() == ReportStatus.RESOLVED) {
            report.resolve(command.operatorMemo());
            // RESOLVED된 경우 블랙리스트 검토
            checkAndPublishBlacklistReviewForResolved(report.getTargetUserId());
        } else if (command.status() == ReportStatus.REJECTED) {
            report.reject(command.operatorMemo());
            // 블라인드 해제 이벤트 발행
            publishUnblindEvent(report);
        }

        return ReportInfoResult.from(report);
    }

    // ============= 검증 메서드 =============

    // 신고 중복 여부 검사
    private void validateDuplicateReport(UUID reporterId, UUID targetId) {
        if (reportRepository.existsByReporterIdAndTargetId(reporterId, targetId)) {
            throw new ReportException(ReportErrorCode.REPORT_EXISTS);
        }
    }

    private void validateReportIsAutoBlinded(Report report) {
        if (report.getStatus() != ReportStatus.AUTO_BLINDED) {
            throw new ReportException(ReportErrorCode.REPORT_ALREADY_PROCESSED);
        }
    }

    private void validateStatusIsValid(ReportStatus status) {
        if (status != ReportStatus.RESOLVED && status != ReportStatus.REJECTED) {
            throw new ReportException(ReportErrorCode.INVALID_STATUS);
        }
    }

    // ============= 저장 및 조회 메서드 =============

    private Report saveReportSafely(Report report) {
        try {
            return reportRepository.save(report);
        } catch (DataIntegrityViolationException e) {
            throw new ReportException(ReportErrorCode.DUPLICATE_REPORT);
        }
    }

    private Report findReportById(UUID reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportException(ReportErrorCode.REPORT_NOT_FOUND));
    }

    private Page<Report> fetchReportsByQuery(ListReportQuery query, Pageable pageable) {
        if (query.status() != null) {
            return reportRepository.findAllByStatus(query.status(), pageable);
        }
        return reportRepository.findAll(pageable);
    }

    // ============= 분산 락 관련 메서드 =============

    // 락 획득
    private boolean acquireDistributedLock(UUID targetId) {
        try {
            boolean lockAcquired = redissonDistributedLock.tryLock(
                    targetId,
                    lockProperties.getWaitTime(),
                    lockProperties.getLeaseTime(),
                    TimeUnit.SECONDS
            );

            if (!lockAcquired) {
                throw new ReportException(ReportErrorCode.LOCK_ACQUISITION_FAILED);
            }

            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ReportException(ReportErrorCode.LOCK_INTERRUPTED);
        }
    }

    // 락 해제
    private void releaseDistributedLock(UUID targetId, boolean lockAcquired) {
        if (lockAcquired) {
            redissonDistributedLock.unlock(targetId);
        }
    }

    // ============= 블라인드 처리 관련 메서드 =============

    private void checkAndProcessAutomaticBlind(CreateReportCommand command, Report report) {
        // saveReportSafely 이후 호출되므로 현재 신고가 이미 포함된 카운트
        long reportCount = reportRepository.countByTargetId(command.targetId());

        if (reportCount == 3) {
            report.blind();
            publishBlindEvent(command);
        }
    }

    private void publishBlindEvent(CreateReportCommand command) {
        String category = null;

        if (command.targetType().name().equals("CHAT")) {
            category = chatClient.getChatMessageWriter(command.targetId()).category();
        }

        outbox.publish(
                "REPORT",
                command.targetId(),
                EventType.REPORT_BLINDED,
                targetBlindedTopic,
                new TargetBlindedEvent(
                        command.targetId(),
                        command.targetType(),
                        command.targetUserId(),
                        category,
                        "누적 신고 3회로 인해 자동 블라인드 처리되었습니다."
                )
        );
    }

    // 블라인드 해제 이벤트 발행 (REJECTED 시)
    private void publishUnblindEvent(Report report) {
        outbox.publish(
                "REPORT",
                report.getTargetId(),
                EventType.REPORT_UNBLINDED,
                targetUnblindedTopic,
                new TargetUnblindedEvent(
                        report.getTargetId(),
                        report.getTargetType(),
                        report.getTargetUserId(),
                        "매니저 검토 결과 오인 신고로 판단되어 블라인드가 해제되었습니다."
                )
        );
    }

    // ============= 블랙리스트 검토 관련 메서드 =============
    // 블랙리스트 검토 로직 (RESOLVED된 게시글만 카운트)
    private void checkAndPublishBlacklistReviewForResolved(UUID targetUserId) {
        long userResolvedCount = reportRepository.countResolvedTargetByUserId(targetUserId);

        if (userResolvedCount >= 5) {
            BlacklistReviewRequestedEvent event = new BlacklistReviewRequestedEvent(
                    targetUserId,
                    "매니저에 의해 확정된 블라인드 처리가 5회 누적되어 블랙리스트 등록 검토가 필요합니다.",
                    userResolvedCount
            );

            outbox.publish(
                    "REPORT",
                    targetUserId,
                    EventType.BLACKLIST_REVIEW_REQUESTED,
                    blacklistReviewRequestedTopic,
                    event
            );
        }
    }
}
