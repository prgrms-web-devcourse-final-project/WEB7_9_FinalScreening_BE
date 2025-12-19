package com.back.matchduo.domain.notification.enums;

public enum NotificationType {
    
    REVIEW_REQUEST,     // 리뷰 작성 요청 (게임 종료)
    NEW_CHAT,     // 신규 채팅 (새로운 채팅 메시지 도착 or 채팅방 생성)
    CHAT_EXIT,        // 채팅 퇴장 (상대방이 나갔을 때)
    RECRUITMENT_COMPLETED // 모집 완료 (내가 참여한 파티 모집 완료 or 내가 쓴 글 모집 완료)
}