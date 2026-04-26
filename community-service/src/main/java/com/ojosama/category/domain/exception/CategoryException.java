package com.ojosama.category.domain.exception;

import com.ojosama.common.exception.CustomException;

public class CategoryException extends CustomException {
    public CategoryException(CategoryErrorCode errorCode) {
        super(errorCode);
    }
}
