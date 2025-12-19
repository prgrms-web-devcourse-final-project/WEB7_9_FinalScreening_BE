package com.back.matchduo.domain.chat.repository;

import com.back.matchduo.domain.chat.entity.ChatRoom;
import com.back.matchduo.domain.user.entity.User;
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

    /** 채팅방 상세 조회 (sender, receiver, post 함께 로드 - N+1 방지) */
    @Query("SELECT r FROM ChatRoom r " +
           "JOIN FETCH r.sender " +
           "JOIN FETCH r.receiver " +
           "JOIN FETCH r.post p " +
           "WHERE r.id = :id")
    Optional<ChatRoom> findByIdWithDetails(@Param("id") Long id);

    /**
     * [파티 영입 후보 조회]
     * 특정 모집글(Post)에 연결된 채팅방 중,
     * 1. 방장(Receiver)이 '나(Leader)'인 방을 찾아서
     * 2. 그 방의 상대방(Sender, 지원자)을 반환함.
     * 3. 단, 상대방이 방을 나갔거나(senderLeft=true),
     * 4. 이미 파티에 가입된 멤버(PartyMember)라면 제외함.
     */
    @Query("SELECT cr.sender FROM ChatRoom cr " +
            "WHERE cr.post.id = :postId " +
            "AND cr.receiver.id = :leaderId " +       // 방장은 나여야 함
            "AND cr.senderLeft = false " +            // 상대가 방을 나갔으면 후보 아님
            "AND cr.sender.id NOT IN (" +             // 이미 파티원인 사람은 제외
            "   SELECT pm.user.id FROM PartyMember pm WHERE pm.party.postId = :postId"+
            ")")
    List<User> findCandidateUsers(@Param("postId") Long postId, @Param("leaderId") Long leaderId);
}