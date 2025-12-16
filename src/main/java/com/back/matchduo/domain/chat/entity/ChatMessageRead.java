package com.back.matchduo.domain.chat.entity;

import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅 읽음 상태 엔티티
 * - 방-유저 단위로 마지막 읽은 메시지 추적
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "chat_message_read",
        uniqueConstraints = {
                // 방-유저당 읽음 상태 1행
                @UniqueConstraint(name = "uk_message_read_room_user", columnNames = {"chat_room_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_message_read_user", columnList = "user_id")
        }
)
public class ChatMessageRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_read_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_read_message_id")
    private ChatMessage lastReadMessage; // 마지막으로 읽은 메시지 포인터 (없으면 NULL)

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;


    public static ChatMessageRead create(ChatRoom chatRoom, User user) {
        if (chatRoom == null || chatRoom.getId() == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_CHAT_ROOM);
        }
        if (user == null || user.getId() == null) {
            throw new CustomException(CustomErrorCode.NOT_FOUND_USER);
        }

        ChatMessageRead state = new ChatMessageRead();
        state.chatRoom = chatRoom;
        state.user = user;
        state.lastReadMessage = null;
        state.lastReadAt = null;
        return state;
    }

    /** 새 세션 시작 시 읽음 상태 초기화 */
    public void resetForNewSession() {
        this.lastReadMessage = null;
        this.lastReadAt = null;
    }

    /** 특정 메시지까지 읽음 처리 */
    public void markReadUpTo(ChatMessage message) {
        if (message == null || message.getId() == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_MESSAGE);
        }
        if (message.getChatRoom() == null || message.getChatRoom().getId() == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_CHAT_ROOM);
        }
        if (this.chatRoom == null || this.chatRoom.getId() == null) {
            throw new CustomException(CustomErrorCode.CHAT_READ_STATE_INVALID);
        }
        if (!this.chatRoom.getId().equals(message.getChatRoom().getId())) {
            throw new CustomException(CustomErrorCode.CHAT_ROOM_MISMATCH);
        }

        Integer messageSessionNo = message.getSessionNo();
        if (messageSessionNo == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_SESSION);
        }

        Integer currentSessionNo = this.chatRoom.getCurrentSessionNo();
        if (currentSessionNo != null) {
            // 현재 세션이 아닌 메시지는 읽음 반영하지 않음
            if (!messageSessionNo.equals(currentSessionNo)) {
                return;
            }
        }

        if (this.lastReadMessage != null && this.lastReadMessage.getId() != null) {
            if (this.lastReadMessage.getId() >= message.getId()) {
                return;
            }
        }

        this.lastReadMessage = message;
        this.lastReadAt = LocalDateTime.now();
    }
}
