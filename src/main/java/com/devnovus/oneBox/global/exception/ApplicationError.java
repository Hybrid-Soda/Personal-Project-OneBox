package com.devnovus.oneBox.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApplicationError implements ErrorCode {
    // common
    MISSING_REQUIRED_HEADER(HttpStatus.BAD_REQUEST, "필수 요청 헤더가 누락되었습니다.", 400),

    // user
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED,"접근 권한이 없습니다.", 401),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"유저가 존재하지 않습니다.", 404),

    // metadata
    METADATA_NOT_FOUND(HttpStatus.NOT_FOUND,"메타데이터를 찾을 수 없습니다.", 404),

    // metadata - folder
    NOT_A_FOLDER(HttpStatus.BAD_REQUEST,"폴더 타입이 아닙니다.", 400),
    FOLDER_PATH_LENGTH_EXCEEDED(HttpStatus.BAD_REQUEST,"경로 길이 제한을 초과했습니다.", 400),
    FOLDER_CHILD_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST,"하위 폴더 최대 개수를 초과했습니다.", 400),
    FOLDER_CANNOT_MOVE_TO_DESCENDANT(HttpStatus.BAD_REQUEST,"자신의 하위 폴더로 이동할 수 없습니다.", 400),
    FOLDER_NOT_ALLOWED_ROOT_MODIFY(HttpStatus.FORBIDDEN,"루트 폴더는 수정할 수 없습니다.", 403),
    FOLDER_NAME_DUPLICATED(HttpStatus.CONFLICT,"동일한 이름의 폴더가 이미 존재합니다.", 409),

    // metadata - file
    NOT_A_FILE(HttpStatus.BAD_REQUEST,"파일 타입이 아닙니다.", 400),
    FILE_INVALID_NAME(HttpStatus.BAD_REQUEST,"파일명이 길이 제한을 초과했거나 허용되지 않은 문자를 포함합니다.", 400),
    FILE_UNSUPPORTED_TYPE(HttpStatus.BAD_REQUEST, "지원되지 않는 파일 형식입니다.", 400),
    FILE_NAME_DUPLICATED(HttpStatus.CONFLICT,"동일한 이름의 파일이 이미 존재합니다.", 409),
    FILE_INSUFFICIENT_STORAGE(HttpStatus.PAYLOAD_TOO_LARGE, "사용 가능한 저장 용량이 부족합니다.", 413),

    E_TAG_NOT_RETURNED(HttpStatus.INTERNAL_SERVER_ERROR,"ETag가 비어있습니다.", 500),
    FILE_NOT_SAVED(HttpStatus.INTERNAL_SERVER_ERROR,"파일 업로드에 실패했습니다.", 500),
    FILE_NOT_DOWNLOADED(HttpStatus.INTERNAL_SERVER_ERROR,"파일 다운로드에 실패했습니다.", 500),
    FILE_NOT_DELETED(HttpStatus.INTERNAL_SERVER_ERROR,"파일 삭제에 실패했습니다.", 500),
    FAIL_TO_GET_URL(HttpStatus.INTERNAL_SERVER_ERROR,"pre-signed url 발급에 실패했습니다.", 500),
    FAIL_TO_COMPLETE_UPLOAD(HttpStatus.INTERNAL_SERVER_ERROR,"파일 업로드 확정에 실패했습니다.", 500),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final Integer code;
}
