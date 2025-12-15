package com.back.matchduo.global.exeption;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final CustomErrorCode errorCode;

    public CustomException(CustomErrorCode errorCode) {
        super(errorCode.getMessage()); // RuntimeException에 메시지 전달 (로그용)
        this.errorCode = errorCode;
    }
}
