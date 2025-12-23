package com.back.matchduo.domain.chat.repository;

import com.back.matchduo.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    /** 커서 기반 페이징 (이전 메시지 불러오기) */
    @Query("SELECT m FROM ChatMessage m " +
            "JOIN FETCH m.sender " +
            "WHERE m.chatRoom.id = :roomId AND m.sessionNo = :sessionNo " +
            "AND (:cursor IS NULL OR m.id < :cursor) " +
            "ORDER BY m.id DESC")
    List<ChatMessage> findByCursorWithSender(
            @Param("roomId") Long roomId,
            @Param("sessionNo") Integer sessionNo,
            @Param("cursor") Long cursor,
            Pageable pageable);

    /** 안 읽은 메시지 수 */
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
           "WHERE m.chatRoom.id = :roomId AND m.sessionNo = :sessionNo AND m.id > :lastReadId")
    long countUnread(
            @Param("roomId") Long roomId,
            @Param("sessionNo") Integer sessionNo,
            @Param("lastReadId") Long lastReadId);

    /** 채팅방의 마지막 메시지 조회 (현재 세션) */
    Optional<ChatMessage> findFirstByChatRoomIdAndSessionNoOrderByIdDesc(Long roomId, Integer sessionNo);

    /** 마지막 메시지 배치 조회 (N+1 방지) **/
    @Query("SELECT m FROM ChatMessage m " +
           "JOIN FETCH m.sender " +
           "WHERE m.chatRoom.id IN :roomIds " +
           "ORDER BY m.chatRoom.id, m.id DESC")
    List<ChatMessage> findRecentByRoomIds(
            @Param("roomIds") List<Long> roomIds);

    /** 특정 채팅방들의 메시지 삭제 **/
    @Modifying
    @Query("DELETE FROM ChatMessage m WHERE m.chatRoom.id IN :roomIds")
    void deleteByRoomIds(@Param("roomIds") List<Long> roomIds);

    /** 오래된 메시지 삭제 (날짜 기준) **/
    @Modifying
    @Query("DELETE FROM ChatMessage m WHERE m.createdAt < :threshold")
    int deleteOldMessages(@Param("threshold") LocalDateTime threshold);

}
