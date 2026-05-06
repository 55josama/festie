package com.ojosama.report.application.service;

import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.OutboxEventPublisher;
import com.ojosama.report.application.dto.command.CreateReportCommand;
import com.ojosama.report.application.dto.command.UpdateReportCommand;
import com.ojosama.report.application.dto.query.ListReportQuery;
import com.ojosama.report.application.dto.result.ReportInfoResult;
import com.ojosama.report.application.dto.result.ReportResult;
import com.ojosama.report.domain.event.payload.BlacklistReviewRequestedEvent;
import com.ojosama.report.domain.event.payload.TargetBlindedEvent;
import com.ojosama.report.domain.exception.ReportErrorCode;
import com.ojosama.report.domain.exception.ReportException;
import com.ojosama.report.domain.model.entity.Report;
import com.ojosama.report.domain.model.enums.ReportStatus;
import com.ojosama.report.domain.model.enums.ReporterType;
import com.ojosama.report.domain.repository.ReportRepository;
import com.ojosama.report.infrastructure.client.ChatClient;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.kafka.topic.report-blinded}")
    private String targetBlindedTopic;

    @Value("${spring.kafka.topic.blacklist-requested}")
    private String blacklistReviewRequestedTopic;

    // 신고 생성
    @Transactional
    public ReportInfoResult createReport(CreateReportCommand command, ReporterType reporterType){
        validateDuplicateReport(command.reporterId(), command.targetId());

        Report savedReport = saveReportSafely(command.toEntity(reporterType));

        checkAndProcessAutomaticBlind(command);

        return ReportInfoResult.from(savedReport);
    }

    // 신고 목록 조회
    public Page<ReportResult> getReportList(ListReportQuery listReportQuery, Pageable pageable) {
        Page<Report> reports = fetchReportsByQuery(listReportQuery, pageable);
        return reports.map(ReportResult::from);
    }

    // 신고 상세 조회
    public ReportInfoResult getReportInfo(UUID reportId) {
        Report report = findReportById(reportId);
        return ReportInfoResult.from(report);
    }

    // 신고 상태 변경 및 운영진 메모 추가
    @Transactional
    public ReportInfoResult updateReport(UUID reportId, UpdateReportCommand command) {
        Report report = findReportById(reportId);
        validateReportIsPending(report);

        if (command.isResolved()) {
            report.resolve(command.operatorMemo());
        } else {
            report.reject(command.operatorMemo());
        }

        return ReportInfoResult.from(report);
    }

    // 신고 중복 여부 검사
    private void validateDuplicateReport(UUID reporterId, UUID targetId) {
        if (reportRepository.existsByReporterIdAndTargetId(reporterId, targetId)) {
            throw new ReportException(ReportErrorCode.REPORT_EXISTS);
        }
    }

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

    private void validateReportIsPending(Report report) {
        if (report.getStatus() != ReportStatus.AUTO_BLINDED) {
            throw new ReportException(ReportErrorCode.REPORT_ALREADY_PROCESSED);
        }
    }

    private Page<Report> fetchReportsByQuery(ListReportQuery query, Pageable pageable) {
        if (query.status() != null) {
            return reportRepository.findAllByStatus(query.status(), pageable);
        }
        return reportRepository.findAll(pageable);
    }

    private void checkAndProcessAutomaticBlind(CreateReportCommand command) {
        long reportCount = reportRepository.countByTargetId(command.targetId());

        // 신고가 3회 이상이면 신고 대상 블라인드 처리 및 유저 블랙리스트 조건 검사
        if (reportCount >= 3) {
            publishBlindEvent(command);
            publishBlacklistReviewRequestEvent(command.targetUserId());
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

    private void publishBlacklistReviewRequestEvent(UUID targetUserId) {
        long userBlindCount = reportRepository.countBlindedTargetByUserId(targetUserId);

        if (userBlindCount >= 5) {
            BlacklistReviewRequestedEvent event = new BlacklistReviewRequestedEvent(
                    targetUserId,
                    "블라인드 처리가 5회 누적되어 블랙리스트 등록 검토가 필요합니다.",
                    userBlindCount
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
