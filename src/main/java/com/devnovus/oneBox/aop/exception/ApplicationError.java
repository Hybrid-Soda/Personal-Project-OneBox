package com.devnovus.oneBox.aop.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApplicationError implements ErrorCode {
    // user
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED,"접근 권한이 없습니다.", 401),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"유저가 존재하지 않습니다.", 404),

    // folder
    IS_NOT_FOLDER(HttpStatus.BAD_REQUEST,"폴더가 아닙니다.", 400),
    PATH_LENGTH_EXCEEDED(HttpStatus.BAD_REQUEST,"경로 문자 길이 제한을 초과했습니다.", 400),
    MOVE_TO_ITSELF_NOT_ALLOWED(HttpStatus.BAD_REQUEST,"자신의 하위 폴더로 이동할 수 없습니다.", 400),
    TOO_MANY_CHILD_FOLDERS(HttpStatus.BAD_REQUEST,"같은 폴더에 생성 가능한 폴더 수를 초과했습니다.", 400),
    ROOT_FOLDER_UPDATE_NOT_ALLOWED(HttpStatus.FORBIDDEN,"루트 폴더는 수정할 수 없습니다.", 403),
    FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND,"폴더가 존재하지 않습니다.", 404),
    DUPLICATE_FOLDER_NAME(HttpStatus.CONFLICT,"같은 이름의 폴더가 이미 존재합니다.", 409),
    ;


    private final HttpStatus httpStatus;
    private final String message;
    private final Integer code;
}
