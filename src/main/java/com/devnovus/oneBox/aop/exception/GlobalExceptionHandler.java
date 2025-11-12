package com.devnovus.oneBox.aop.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        String error = exception.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).toList().get(0);
        return ResponseEntity.status(400).body(ValidErrorResponse.of(error));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException exception) {
        ApplicationError error = exception.getError();

        return ResponseEntity.status(error.getHttpStatus()).body(ErrorResponse.of(error));
    }
}
