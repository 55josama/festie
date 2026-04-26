package com.ojosama.operationservice.application.service;

import com.ojosama.common.exception.CustomException;
import com.ojosama.operationservice.application.dto.command.CreateReportCommand;
import com.ojosama.operationservice.application.dto.command.UpdateReportCommand;
import com.ojosama.operationservice.application.dto.query.ListReportQuery;
import com.ojosama.operationservice.application.dto.result.ReportInfoResult;
import com.ojosama.operationservice.application.dto.result.ReportResult;
import com.ojosama.operationservice.domain.event.ReportEventProducer;
import com.ojosama.operationservice.domain.event.payload.BlacklistRegisterEvent;
import com.ojosama.operationservice.domain.event.payload.TargetBlindEvent;
import com.ojosama.operationservice.domain.exception.ReportErrorCode;
import com.ojosama.operationservice.domain.exception.ReportException;
import com.ojosama.operationservice.domain.model.entity.Report;
import com.ojosama.operationservice.domain.model.enums.ReportStatus;
import com.ojosama.operationservice.domain.model.enums.ReporterType;
import com.ojosama.operationservice.domain.repository.ReportRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final ReportEventProducer reportEventProducer;

    // 신고 생성
    @Transactional
    public ReportInfoResult createReport(CreateReportCommand command, ReporterType reporterType){
        validateDuplicateReport(command.getReporterId(), command.getTargetId());

        Report savedReport = saveReportSafely(command.toEntity(reporterType));

        checkAndProcessAutomaticBlind(command);

        return ReportInfoResult.from(savedReport);
    }

    // 신고 목록 조회
    public Page<ReportResult> getReports(ListReportQuery listReportQuery, Pageable pageable) {
        Page<Report> reports = fetchReportsByQuery(listReportQuery, pageable);
        return reports.map(ReportResult::from);
    }

    // 신고 상세 조회
    public ReportInfoResult getReportDetail(UUID reportId) {
        Report report = findReportById(reportId);
        return ReportInfoResult.from(report);
    }

    // 신고 상태 변경 및 운영진 메모 추가
    @Transactional
    public ReportInfoResult updateReport(UUID reportId, UpdateReportCommand command) {
        Report report = findReportById(reportId);
        validateReportIsPending(report);

        if (command.getIsResolved()) {
            report.resolve(command.getOperatorMemo());
        } else {
            report.reject(command.getOperatorMemo());
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
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new ReportException(ReportErrorCode.REPORT_ALREADY_PROCESSED);
        }
    }

    private Page<Report> fetchReportsByQuery(ListReportQuery query, Pageable pageable) {
        if (query.getStatus() != null) {
            return reportRepository.findAllByStatus(query.getStatus(), pageable);
        }
        return reportRepository.findAll(pageable);
    }

    private void checkAndProcessAutomaticBlind(CreateReportCommand command) {
        long reportCount = reportRepository.countByTargetId(command.getTargetId());

        // 신고가 3회 이상이면 신고 대상 블라인드 처리 및 유저 블랙리스트 조건 검사
        if (reportCount >= 3) {
            publishBlindEvent(command);
            checkBlacklistCondition(command.getTargetUserId());
        }
    }

    private void publishBlindEvent(CreateReportCommand command) {
        String role = command.getTargetType().name().equals("CHAT")
                ? "CATEGORY_MANAGER" : "COMMUNITY_MANAGER";

        reportEventProducer.publishTargetBlindEvent(new TargetBlindEvent(
                command.getTargetId(),
                command.getTargetType().name(),
                role,
                "누적 신고 3회로 인해 자동 블라인드 처리되었습니다."
        ));
    }

    private void checkBlacklistCondition(UUID targetUserId) {
        long userBlindCount = reportRepository.countBlindedTargetByUserId(targetUserId);

        if (userBlindCount >= 5) {
            reportEventProducer.publishBlacklistRegisterEvent(new BlacklistRegisterEvent(
                    targetUserId,
                    (int) userBlindCount,
                    "블라인드 처리가 5회 누적되어 블랙리스트 등록 검토가 필요합니다."
            ));
        }
    }
}
