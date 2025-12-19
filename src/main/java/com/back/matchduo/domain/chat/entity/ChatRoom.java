package com.back.matchduo.domain.chat.entity;

import com.back.matchduo.domain.post.entity.Post;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 1:1 채팅방 엔티티
 * - 모집글(Post) 기반으로 생성
 * - 세션 기반 메시지 관리 (재입장 시 과거 메시지 숨김)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "chat_room",
        uniqueConstraints = {
                // 같은 모집글 + 같은 신청자 조합 => 채팅방 1개만 (멱등)
                @UniqueConstraint(name = "uk_chat_room_post_sender", columnNames = {"post_id", "sender_id"})
        },
        indexes = {
                @Index(name = "idx_chat_room_post", columnList = "post_id"),
                @Index(name = "idx_chat_room_sender", columnList = "sender_id"),
                @Index(name = "idx_chat_room_receiver", columnList = "receiver_id")
        }
)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver; // 방장: 모집글 작성자, 채팅 요청을 받는 측

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender; // 지원자: 채팅방 생성자, 채팅 요청을 보낸 측

    @Column(name = "sender_left", nullable = false)
    private boolean senderLeft = false;

    @Column(name = "receiver_left", nullable = false)
    private boolean receiverLeft = false;

    @Column(name = "current_session_no", nullable = false)
    private Integer currentSessionNo = 1;

    @Column(name = "session_started_at", nullable = false)
    private LocalDateTime sessionStartedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        if (currentSessionNo == null) currentSessionNo = 1;
        if (sessionStartedAt == null) sessionStartedAt = LocalDateTime.now();
    }

    public static ChatRoom create(Post post, User receiver, User sender) {
        if (post == null || post.getId() == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_CHAT_ROOM);
        }
        if (receiver == null || receiver.getId() == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_USER_ID);
        }
        if (sender == null || sender.getId() == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_USER_ID);
        }
        if (receiver.getId().equals(sender.getId())) {
            throw new CustomException(CustomErrorCode.CHAT_SAME_SENDER_RECEIVER);
        }

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.post = post;
        chatRoom.receiver = receiver;
        chatRoom.sender = sender;
        chatRoom.receiverLeft = false;
        chatRoom.senderLeft = false;
        chatRoom.currentSessionNo = 1;
        chatRoom.sessionStartedAt = LocalDateTime.now();
        return chatRoom;
    }

    /** 채팅방 나가기 */
    public void leave(Long userId) {
        if (userId == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_USER_ID);
        }
        // 멤버가 아닌 userId로 leave() 호출되는 경우를 방지하기 위해 방 참여 여부를 추적
        boolean inRoom = false;

        if (isSender(userId)) {
            senderLeft = true;
            inRoom = true;
        }
        if (isReceiver(userId)) {
            receiverLeft = true;
            inRoom = true;
        }

        // sender/receiver 둘 다 아니면 예외 처리
        if (!inRoom) {
            throw new CustomException(CustomErrorCode.CHAT_USER_NOT_IN_ROOM);
        }
    }

    /** 채팅방이 닫혔는지 확인 (한 쪽이라도 나갔으면 닫힘) */
    public boolean isClosed() {
        return senderLeft || receiverLeft;
    }

    /** 채팅방이 완전히 닫혔는지 (양쪽 모두 나감) */
    public boolean isFullyClosed() {
        return senderLeft && receiverLeft;
    }

    /** 채팅방이 열려있는지 (양쪽 모두 참여 중) */
    public boolean isOpen() {
        return !senderLeft && !receiverLeft;
    }

    /** 채팅방 활성 상태 (양쪽 모두 참여 중) */
    public boolean isActive() {
        return isOpen();
    }

    /** 같은 채팅방을 재사용하되 세션을 증가시켜 과거 메시지를 숨김 */
    public void resumeAsNewSession() {
        // 이미 열려있는 방이면 세션 재개 불가
        if (isOpen()) {
            throw new CustomException(CustomErrorCode.CHAT_ROOM_ALREADY_OPEN);
        }
        // 여기서는 닫힌 방(한 명 이상 나감)만 재개 대상으로 간주
        this.senderLeft = false;
        this.receiverLeft = false;

        if (this.currentSessionNo == null) this.currentSessionNo = 1;
        this.currentSessionNo += 1;
        this.sessionStartedAt = LocalDateTime.now();
    }

    /** 유저가 receiver(방장)인지 확인 */
    public boolean isReceiver(Long userId) {
        return receiver != null && receiver.getId() != null && receiver.getId().equals(userId);
    }

    /** 유저가 sender(지원자)인지 확인 */
    public boolean isSender(Long userId) {
        return sender != null && sender.getId() != null && sender.getId().equals(userId);
    }

    /** 유저의 역할 반환 */
    public ChatMemberRole getMemberRole(Long userId) {
        if (isReceiver(userId)) return ChatMemberRole.RECEIVER;
        if (isSender(userId)) return ChatMemberRole.SENDER;
        throw new CustomException(CustomErrorCode.CHAT_USER_NOT_IN_ROOM);
    }
}
