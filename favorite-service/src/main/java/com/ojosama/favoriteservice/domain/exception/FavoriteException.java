package com.ojosama.favoriteservice.domain.exception;

import com.ojosama.common.exception.CustomException;

public class FavoriteException extends CustomException {

    public FavoriteException(FavoriteErrorCode code) {
        super(code);
    }
}
