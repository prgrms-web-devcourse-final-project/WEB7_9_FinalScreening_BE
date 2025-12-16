package com.back.matchduo.domain.chat.repository;

import com.back.matchduo.domain.chat.entity.ChatMessageRead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatMessageReadRepository extends JpaRepository<ChatMessageRead, Long> {
    // 방-유저 조합으로 읽음 상태 조회
    Optional<ChatMessageRead> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);
}
