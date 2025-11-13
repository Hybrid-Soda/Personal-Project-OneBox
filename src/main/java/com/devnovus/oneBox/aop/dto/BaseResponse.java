package com.devnovus.oneBox.aop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private ResponseCode code = ResponseCode.SUCCESS;
    private String message = "";
    private T result;

    public static <T> BaseResponse<T> of(String message) {
        return new BaseResponse<>(ResponseCode.SUCCESS, message, null);
    }

    public static <T> BaseResponse<T> of(T result) {
        return new BaseResponse<>(ResponseCode.SUCCESS, null, result);
    }
}
