package com.devnovus.oneBox.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private String code = "SUCCESS";
    private String message = "";
    private T result;

    public static <T> BaseResponse<T> of(String message) {
        return new BaseResponse<>("SUCCESS", message, null);
    }

    public static <T> BaseResponse<T> of(T result) {
        return new BaseResponse<>("SUCCESS", null, result);
    }
}
