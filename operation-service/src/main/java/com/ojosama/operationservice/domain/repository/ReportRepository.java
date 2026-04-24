package com.ojosama.operationservice.domain.repository;

import com.ojosama.operationservice.domain.model.entity.Report;

public interface ReportRepository {
    Report save(Report report);
}
