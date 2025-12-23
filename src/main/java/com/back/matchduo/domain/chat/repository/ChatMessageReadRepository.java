package com.back.matchduo.domain.chat.repository;

import com.back.matchduo.domain.chat.entity.ChatMessageRead;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageReadRepository extends JpaRepository<ChatMessageRead, Long> {
    /** 방-유저 조합으로 읽음 상태 조회 */
    Optional<ChatMessageRead> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    /** 새 세션 시작 시 읽음 상태 벌크 초기화 */
    @Modifying
    @Query("UPDATE ChatMessageRead r " +
           "SET r.lastReadMessage = NULL, r.lastReadAt = NULL " +
           "WHERE r.chatRoom.id = :chatRoomId")
    void resetAllForRoom(
            @Param("chatRoomId") Long chatRoomId);

    /** 읽음 상태 업데이트 시 비관적 락 조회 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ChatMessageRead r " +
           "WHERE r.chatRoom.id = :chatRoomId " +
           "AND r.user.id = :userId")
    Optional<ChatMessageRead> findByChatRoomIdAndUserIdWithLock(
            @Param("chatRoomId") Long chatRoomId,
            @Param("userId") Long userId);

    /** 읽음 상태 배치 조회 (N+1 방지) **/
    @Query("SELECT r FROM ChatMessageRead r " +
           "WHERE r.chatRoom.id IN :roomIds AND r.user.id = :userId")
    List<ChatMessageRead> findByRoomIdsAndUserId(
            @Param("roomIds") List<Long> roomIds,
            @Param("userId") Long userId);

    /** 특정 채팅방들의 읽음 상태 삭제 **/
    @Modifying
    @Query("DELETE FROM ChatMessageRead r WHERE r.chatRoom.id IN :roomIds")
    void deleteByRoomIds(@Param("roomIds") List<Long> roomIds);
}
