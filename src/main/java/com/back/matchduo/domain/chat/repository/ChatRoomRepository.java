package com.back.matchduo.domain.chat.repository;

import com.back.matchduo.domain.chat.entity.ChatRoom;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    /** 모집글 + 신청자 조합으로 채팅방 조회 (멱등 처리용) */
    Optional<ChatRoom> findByPostIdAndSenderId(Long postId, Long senderId);

    /** 내 채팅방 목록 조회 (커서 기반 페이징, N+1 방지용 JOIN FETCH) */
    @Query("SELECT r FROM ChatRoom r " +
           "JOIN FETCH r.post p " +
           "JOIN FETCH p.gameMode " +
           "JOIN FETCH r.receiver JOIN FETCH r.sender " +
           "WHERE (r.receiver.id = :userId OR r.sender.id = :userId) " +
           "AND p.isActive = true " +
           "AND NOT (r.senderLeft = true AND r.receiverLeft = true) " +
           "AND (:cursor IS NULL OR r.id < :cursor) " +
           "ORDER BY r.id DESC")
    List<ChatRoom> findMyRooms(@Param("userId") Long userId,
                               @Param("cursor") Long cursor,
                               Pageable pageable);

    /** 채팅방 나가기 시 비관적 락 조회 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ChatRoom r WHERE r.id = :id")
    Optional<ChatRoom> findByIdWithLock(@Param("id") Long id);

    /** 닫힌 방 재활성화 시에만 락 사용 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ChatRoom r " +
           "WHERE r.post.id = :postId " +
           "AND r.sender.id = :senderId")
    Optional<ChatRoom> findByPostIdAndSenderIdWithLock(
            @Param("postId") Long postId,
            @Param("senderId") Long senderId);

    /** WebSocket 구독 시 채팅방 멤버 검증용 */
    @Query("SELECT COUNT(r) > 0 FROM ChatRoom r " +
           "WHERE r.id = :chatId " +
           "AND (r.sender.id = :userId OR r.receiver.id = :userId)")
    boolean existsByIdAndMember(
            @Param("chatId") Long chatId,
            @Param("userId") Long userId);
}