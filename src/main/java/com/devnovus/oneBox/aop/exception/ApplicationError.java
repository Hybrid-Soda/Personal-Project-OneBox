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
    NOT_A_FOLDER(HttpStatus.BAD_REQUEST,"폴더 타입이 아닙니다.", 400),
    FOLDER_PATH_LENGTH_EXCEEDED(HttpStatus.BAD_REQUEST,"경로 길이 제한을 초과했습니다.", 400),
    FOLDER_CHILD_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST,"하위 폴더 최대 개수를 초과했습니다.", 400),
    FOLDER_CANNOT_MOVE_TO_DESCENDANT(HttpStatus.BAD_REQUEST,"자신의 하위 폴더로 이동할 수 없습니다.", 400),
    FOLDER_NOT_ALLOWED_ROOT_MODIFY(HttpStatus.FORBIDDEN,"루트 폴더는 수정할 수 없습니다.", 403),
    FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND,"폴더를 찾을 수 없습니다.", 404),
    FOLDER_NAME_DUPLICATED(HttpStatus.CONFLICT,"동일한 이름의 폴더가 이미 존재합니다.", 409),
    ;


    private final HttpStatus httpStatus;
    private final String message;
    private final Integer code;
}
