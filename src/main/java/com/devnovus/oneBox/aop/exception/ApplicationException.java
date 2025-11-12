package com.devnovus.oneBox.aop.exception;

import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {

    private final ApplicationError error;

    public ApplicationException(ApplicationError error) {
        super(error.getMessage());
        this.error = error;
    }

    public ApplicationException(Throwable cause, ApplicationError error) {
        super(error.getMessage(), cause);
        this.error = error;
    }
}
