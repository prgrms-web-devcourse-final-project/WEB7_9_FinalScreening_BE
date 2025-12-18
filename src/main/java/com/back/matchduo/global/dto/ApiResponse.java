package com.back.matchduo.global.dto;

import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import org.springframework.http.HttpStatus;

public record ApiResponse<T>(
        HttpStatus status,
        String message,
        T data
) {
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(HttpStatus.OK, message, data);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ok("성공적으로 처리되었습니다.", data);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(HttpStatus.CREATED, message, data);
    }

    public static <T> ApiResponse<T> noContent(String message) {
        return new ApiResponse<>(HttpStatus.NO_CONTENT, message, null);
    }

    // ★ 수정된 부분: CustomException 처리
    public static ApiResponse<?> fail(CustomException customException) {
        // getCustomErrorCode() -> getErrorCode()로 수정
        CustomErrorCode errorCode = customException.getErrorCode();
        return new ApiResponse<>(
                errorCode.getStatus(),
                errorCode.getMessage(),
                null
        );
    }

    public static ApiResponse<?> fail(HttpStatus status, String message) {
        return new ApiResponse<>(
                status,
                message,
                null
        );
    }
}