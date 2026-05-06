package com.ojosama.report.domain.repository;

import com.ojosama.report.domain.model.entity.Report;
import com.ojosama.report.domain.model.enums.ReportStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportRepository {
    Report save(Report report);

    Optional<Report> findById(UUID id);

    Page<Report> findAll(Pageable pageable);

    Page<Report> findAllByStatus(ReportStatus status, Pageable pageable);

    boolean existsByReporterIdAndTargetId(UUID reporterId, UUID targetId);

    long countByTargetId(UUID targetId);

    long countResolvedTargetByUserId(UUID targetUserId);
}
