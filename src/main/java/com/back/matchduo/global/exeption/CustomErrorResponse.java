package com.back.matchduo.global.exeption;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomErrorResponse {

    private int status;          // HTTP 상태 코드 (예: 400, 404)
    private String code;         // 커스텀 에러 코드명 (예: "PARTY_FULL")
    private String message;      // 친절한 메시지 (예: "파티 정원이 꽉 찼습니다.")

    @Builder.Default
    private String timestamp = LocalDateTime.now().toString(); // 발생 시간
}