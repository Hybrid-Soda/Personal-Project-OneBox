package com.devnovus.oneBox.aop.exception;

import org.springframework.http.HttpStatus;

public record ValidErrorResponse(HttpStatus httpStatus, String message, Integer code) {

    public static ValidErrorResponse of(String message) {
        return new ValidErrorResponse(HttpStatus.BAD_REQUEST, message, 400);
    }
}
