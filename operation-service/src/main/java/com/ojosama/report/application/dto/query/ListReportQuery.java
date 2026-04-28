package com.ojosama.report.application.dto.query;

import com.ojosama.report.domain.model.enums.ReportStatus;

public record ListReportQuery (
        ReportStatus status
){ }
