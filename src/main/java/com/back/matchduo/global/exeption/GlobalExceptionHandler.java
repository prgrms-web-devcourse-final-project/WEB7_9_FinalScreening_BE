package com.back.matchduo.global.exeption;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CustomErrorResponse> handleCustomException(
            CustomException e,
            HttpServletRequest request
    ) {
        // 1. 에러 로그 기록 (어떤 에러가, 어떤 URL에서 났는지 확인)
        log.error("ErrorCode: {}, URL: {}, Message: {}",
                e.getErrorCode(), request.getRequestURI(), e.getMessage());

        // 2. 응답 DTO 생성
        CustomErrorResponse response = CustomErrorResponse.builder()
                .status(e.getErrorCode().getStatus().value()) // 400, 404 등 숫자
                .code(e.getErrorCode().name())                // "PARTY_FULL" 등 문자열
                .message(e.getErrorCode().getMessage())       // 메시지
                .build();

        // 3. ResponseEntity로 감싸서 반환 (중요: HTTP Status Code 설정)
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(response);
    }
}
