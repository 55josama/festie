package com.ojosama.operationservice.application.dto.query;

import com.ojosama.operationservice.domain.model.enums.ReportStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ListReportQuery {
    private ReportStatus status;
}
