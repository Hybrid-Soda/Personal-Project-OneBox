package com.devnovus.oneBox.aop.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String name();
    HttpStatus getHttpStatus();
    String getMessage();
    Integer getCode();
}
