package com.ojosama.operationservice.application.dto.query;

import com.ojosama.operationservice.domain.model.enums.ReportStatus;

public record ListReportQuery (
        ReportStatus status
){ }
