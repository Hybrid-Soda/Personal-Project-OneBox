package com.devnovus.oneBox.aop.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApplicationError implements ErrorCode {

    // createFolder
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED,"접근 권한이 없습니다.", HttpStatus.UNAUTHORIZED.value()),
    DUPLICATE_FOLDER_NAME(HttpStatus.CONFLICT,"같은 이름의 폴더가 이미 존재합니다.", HttpStatus.CONFLICT.value()),
    PARENT_FOLDER_NOT_FOUND(HttpStatus.BAD_REQUEST,"상위 폴더가 존재하지 않습니다.", HttpStatus.BAD_REQUEST.value()),
    INVALID_FOLDER_NAME(HttpStatus.BAD_REQUEST,"폴더명에 허용되지 않는 특수 문자가 포함되어 있습니다.", HttpStatus.BAD_REQUEST.value()),
    TOO_MANY_CHILD_FOLDERS(HttpStatus.BAD_REQUEST,"같은 폴더에 생성 가능한 폴더 수를 초과했습니다.", HttpStatus.BAD_REQUEST.value()),
    FOLDER_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST,"폴더 깊이 제한을 초과했습니다.", HttpStatus.BAD_REQUEST.value()),
    ;


    private final HttpStatus httpStatus;
    private final String message;
    private final Integer code;
}
