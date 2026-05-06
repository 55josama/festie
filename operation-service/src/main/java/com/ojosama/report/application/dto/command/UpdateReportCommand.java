package com.ojosama.report.application.dto.command;

import com.ojosama.report.domain.model.enums.ReportStatus;

public record UpdateReportCommand(ReportStatus status, String operatorMemo) { }
