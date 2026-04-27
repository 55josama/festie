package com.ojosama.aiservice.domain;

import com.ojosama.report.domain.model.enums.ReportCategory;

public record ModerationResult(
        boolean isViolating,
        ReportCategory category,
        String reason
) { }
