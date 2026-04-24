package com.ojosama.operationservice.infrastructure.persistence;

import com.ojosama.operationservice.domain.model.entity.Report;
import com.ojosama.operationservice.domain.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepository {
    private final ReportJpaRepository reportJpaRepository;

    @Override
    public Report save(Report report){
        return reportJpaRepository.save(report);
    }
}
