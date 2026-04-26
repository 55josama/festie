package com.ojosama.operationservice.infrastructure.persistence;

import com.ojosama.operationservice.domain.model.entity.Report;
import com.ojosama.operationservice.domain.model.enums.ReportStatus;
import com.ojosama.operationservice.domain.repository.ReportRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepository {
    private final ReportJpaRepository reportJpaRepository;

    @Override
    public Report save(Report report){
        return reportJpaRepository.save(report);
    }

    @Override
    public Optional<Report> findById(UUID id){
        return reportJpaRepository.findById(id);
    };

    @Override
    public Page<Report> findAll(Pageable pageable){
        return reportJpaRepository.findAll(pageable);
    };

    @Override
    public Page<Report> findAllByStatus(ReportStatus status, Pageable pageable){
        return reportJpaRepository.findAllByStatus(status, pageable);
    };

    @Override
    public boolean existsByReporterIdAndTargetId(UUID reporterId, UUID targetId){
        return reportJpaRepository.existsByReporterIdAndTargetId(reporterId, targetId);
    };

    @Override
    public long countByTargetId(UUID targetId){
        return reportJpaRepository.countByTargetId(targetId);
    };

    @Override
    public long countBlindedTargetByUserId(UUID targetUserId){
        return reportJpaRepository.countBlindedTargetByUserId(targetUserId);
    };
}
