package com.back.matchduo.global.exeption;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CustomErrorCode {

    // 1. Common (공통)
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 2. User (사용자)
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    NOT_FOUND_NICKNAME(HttpStatus.NOT_FOUND, "작성자를 찾을 수 없습니다."),
    NOT_FOUND_EMAIL(HttpStatus.NOT_FOUND, "이메일이 존재하지 않습니다."),
    WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "로그인된 사용자가 없습니다."),
    USER_STATUS_NOT_BLANK(HttpStatus.BAD_REQUEST, "유저 상태는 공백일 수 없습니다."),
    DUPLICATE_STATUS(HttpStatus.BAD_REQUEST, "동일한 상태로 변경할 수 없습니다."),
    INVALID_USER_ROLE(HttpStatus.FORBIDDEN, "권한이 없는 사용자입니다."),
    ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN, "정지 상태인 계정입니다."),
    ACCOUNT_DEACTIVATED(HttpStatus.FORBIDDEN, "비활성화된 계정입니다."),
    ACCOUNT_BANNED(HttpStatus.FORBIDDEN, "영구 정지된 계정입니다."),
    SELF_INFORMATION(HttpStatus.UNAUTHORIZED, "본인 정보만 수정할 수 있습니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 인증 코드입니다."),
    EXPIRED_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증 코드가 만료되었습니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "이메일 인증이 완료되지 않았습니다."),

    // 3. Party (파티)
    PARTY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 파티를 찾을 수 없습니다."),
    PARTY_ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "이미 종료된 파티입니다."),
    PARTY_ALREADY_JOINED(HttpStatus.BAD_REQUEST, "이미 참여 중인 파티입니다."),
    PARTY_IS_FULL(HttpStatus.BAD_REQUEST, "파티 정원이 초과되었습니다."),
    NOT_PARTY_LEADER(HttpStatus.FORBIDDEN, "파티장만 접근할 수 있는 권한입니다."),
    PARTY_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "파티 멤버 정보를 찾을 수 없습니다."),
    ALREADY_JOINED_PARTY(HttpStatus.BAD_REQUEST, "이미 참여 중인 파티입니다."),

    // 4. Post (모집글) - 추후 구현 시 사용
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 모집글을 찾을 수 없습니다."),

    // 5. Chat (채팅)
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_CLOSED(HttpStatus.BAD_REQUEST, "종료된 채팅방에는 메시지를 보낼 수 없습니다."),
    CHAT_SAME_SENDER_RECEIVER(HttpStatus.BAD_REQUEST, "채팅방의 sender와 receiver는 동일할 수 없습니다."),
    CHAT_INVALID_USER_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 ID입니다."),
    CHAT_INVALID_CHAT_ROOM(HttpStatus.BAD_REQUEST, "채팅방 정보가 올바르지 않습니다."),
    CHAT_INVALID_SENDER(HttpStatus.BAD_REQUEST, "발신자 정보가 올바르지 않습니다."),
    CHAT_INVALID_MESSAGE_TYPE(HttpStatus.BAD_REQUEST, "메시지 타입이 올바르지 않습니다."),
    CHAT_INVALID_MESSAGE_CONTENT(HttpStatus.BAD_REQUEST, "메시지 내용은 비어 있을 수 없습니다."),
    CHAT_INVALID_MESSAGE(HttpStatus.BAD_REQUEST, "메시지 정보가 올바르지 않습니다."),
    CHAT_ROOM_MISMATCH(HttpStatus.BAD_REQUEST, "다른 채팅방의 메시지로 읽음 처리를 할 수 없습니다."),
    CHAT_INVALID_SESSION(HttpStatus.BAD_REQUEST, "메시지의 세션 정보가 올바르지 않습니다."),
    CHAT_READ_STATE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "읽음 상태의 채팅방 정보가 올바르지 않습니다."),
    CHAT_USER_NOT_IN_ROOM(HttpStatus.FORBIDDEN, "해당 채팅방의 참여자가 아닙니다.");

    private final HttpStatus status;
    private final String message;
}
