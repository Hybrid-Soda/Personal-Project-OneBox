package com.devnovus.oneBox.aop.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApplicationError implements ErrorCode {
    // user
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"유저가 존재하지 않습니다.", HttpStatus.NOT_FOUND.value()),

    // createFolder
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED,"접근 권한이 없습니다.", HttpStatus.UNAUTHORIZED.value()),
    DUPLICATE_FOLDER_NAME(HttpStatus.CONFLICT,"같은 이름의 폴더가 이미 존재합니다.", HttpStatus.CONFLICT.value()),
    FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND,"폴더가 존재하지 않습니다.", HttpStatus.NOT_FOUND.value()),
    IS_NOT_FOLDER(HttpStatus.BAD_REQUEST,"폴더가 아닙니다.", HttpStatus.BAD_REQUEST.value()),
    INVALID_FOLDER_NAME(HttpStatus.BAD_REQUEST,"폴더명에 허용되지 않는 특수 문자가 포함되어 있습니다.", HttpStatus.BAD_REQUEST.value()),
    TOO_MANY_CHILD_FOLDERS(HttpStatus.BAD_REQUEST,"같은 폴더에 생성 가능한 폴더 수를 초과했습니다.", HttpStatus.BAD_REQUEST.value()),
    PATH_LENGTH_EXCEEDED(HttpStatus.BAD_REQUEST,"경로 문자 길이 제한을 초과했습니다.", HttpStatus.BAD_REQUEST.value()),
    ;


    private final HttpStatus httpStatus;
    private final String message;
    private final Integer code;
}
