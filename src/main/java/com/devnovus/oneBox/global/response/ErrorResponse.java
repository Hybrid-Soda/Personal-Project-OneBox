package com.devnovus.oneBox.global.response;

import com.devnovus.oneBox.global.exception.ApplicationError;
import org.springframework.http.HttpStatus;

public record ErrorResponse(HttpStatus httpStatus, String message, Integer code) {

    public static ErrorResponse of(ApplicationError error) {
        return new ErrorResponse(error.getHttpStatus(), error.getMessage(), error.getCode());
    }
}
