package com.ojosama.report.infrastructure.persistence;

import com.ojosama.report.domain.model.entity.Report;
import com.ojosama.report.domain.model.enums.ReportStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportJpaRepository extends JpaRepository<Report, UUID> {
    Optional<Report> findById(UUID id);

    Page<Report> findAll(Pageable pageable);

    Page<Report> findAllByStatus(ReportStatus status, Pageable pageable);

    boolean existsByReporterIdAndTargetId(UUID reporterId, UUID targetId);

    Long countByTargetId(UUID targetId);

    @Query("SELECT COUNT(DISTINCT r.targetId) FROM Report r " +
            "WHERE r.targetUserId = :targetUserId " +
            "AND (SELECT COUNT(r2) FROM Report r2 WHERE r2.targetId = r.targetId) >= 3")
    Long countBlindedTargetByUserId(@Param("targetUserId") UUID targetUserId);
}
