package com.back.matchduo.domain.chat.dto.internal;

import com.back.matchduo.domain.chat.entity.ChatRoom;
import com.back.matchduo.domain.gameaccount.entity.GameAccount;

/**
 * 채팅방 상세 정보와 상대방 게임 계정을 함께 담는 내부 DTO
 * - Service 레이어에서 중복 조회를 방지하기 위해 사용
 * - API 요청/응답이 아닌 Service 내부 전달용
 */
public record ChatRoomDetailWithGameAccount(
        ChatRoom room,
        GameAccount otherGameAccount
) {}
