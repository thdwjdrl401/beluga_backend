package com.thdwjdrl.yejeong.beluga.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SuccessResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> SuccessResponse<T> success(T data){
        return new SuccessResponse<>(true, "요청이 성공했습니다.", data);
    }

    public static <T> SuccessResponse<T> success(String message, T data){
        return new SuccessResponse<>(true, message, data);
    }
}
