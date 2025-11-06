package com.tc.common.http;

import lombok.Data;

/**
 * Author jiangzhou
 * Date 2023/10/9
 * Description TODO
 **/
@Data
public class DataResponse<T> {
    private int code;
    private String message;
    private T data;

    public DataResponse(T data) {
        this.code = 200;
        this.message = "成功";
        this.data = data;
    }

    public DataResponse() {
        this.code = 200;
        this.message = "成功";
        this.data = null;
    }

    public DataResponse(ErrorCode errorCode, T data) {
        this.code = errorCode.code;
        this.message = errorCode.message;
        this.data = data;
    }

    public DataResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> DataResponse<T> success(T data) {
        return new DataResponse<>(data);
    }

    public static <T> DataResponse<T> success() {
        return new DataResponse<>();
    }

    public static <T> DataResponse<T> fail(int errorCode, String message, T data) {
        return new DataResponse<>(errorCode, message ,data);
    }
}
