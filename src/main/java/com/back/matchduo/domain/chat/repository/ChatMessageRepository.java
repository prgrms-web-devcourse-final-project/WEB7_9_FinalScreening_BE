package com.back.matchduo.domain.chat.repository;

import com.back.matchduo.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 메시지 목록 (커서 페이징)
    @Query("SELECT m FROM ChatMessage m " +
           "WHERE m.chatRoom.id = :roomId AND m.sessionNo = :sessionNo " +
           "ORDER BY m.id DESC")
    Page<ChatMessage> findMessages(
            @Param("roomId") Long roomId,
            @Param("sessionNo") Integer sessionNo,
            Pageable pageable);

    // 안 읽은 메시지 수
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
           "WHERE m.chatRoom.id = :roomId AND m.sessionNo = :sessionNo AND m.id > :lastReadId")
    long countUnread(
            @Param("roomId") Long roomId,
            @Param("sessionNo") Integer sessionNo,
            @Param("lastReadId") Long lastReadId);

    // 마지막 메시지
    @Query("SELECT m FROM ChatMessage m " +
           "WHERE m.chatRoom.id = :roomId AND m.sessionNo = :sessionNo " +
           "ORDER BY m.id DESC LIMIT 1")
    Optional<ChatMessage> findLastMessage(
            @Param("roomId") Long roomId,
            @Param("sessionNo") Integer sessionNo);
}
