package com.back.matchduo.global.exeption;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final CustomErrorCode errorCode;

    public CustomException(CustomErrorCode errorCode) {
        super(errorCode.getMessage()); // RuntimeException에 메시지 전달 (로그용)
        this.errorCode = errorCode;
    }

    public CustomException(CustomErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause); // 원인 예외를 함께 전달
        this.errorCode = errorCode;
    }
}
