package com.ojosama.report.domain.exception;

import com.ojosama.common.exception.CustomException;
import com.ojosama.common.exception.ErrorCode;

public class ReportException extends CustomException {
    public ReportException(ErrorCode errorCode) {
        super(errorCode);
    }
}
