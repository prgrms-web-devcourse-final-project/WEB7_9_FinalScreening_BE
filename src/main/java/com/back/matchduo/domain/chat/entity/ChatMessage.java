package com.back.matchduo.domain.chat.entity;

import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 엔티티
 * - sessionNo로 세션별 메시지 분리
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "chat_message",
        indexes = {
                // 무한스크롤(커서/범위조회) + 세션 필터링
                @Index(name = "idx_chat_message_room_session_message_id", columnList = "chat_room_id, session_no, chat_message_id"),
                @Index(name = "idx_chat_message_room_session_created_at", columnList = "chat_room_id, session_no, created_at")
        }
)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // 메시지 정렬/무한스크롤(커서)
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "session_no", nullable = false)
    private Integer sessionNo;

    @PrePersist
    private void prePersist() {
        if (sessionNo == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_SESSION);
        }
    }

    public static ChatMessage create(ChatRoom room, User sender, MessageType type, String content) {
        if (room == null || room.getId() == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_CHAT_ROOM);
        }
        if (sender == null || sender.getId() == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_SENDER);
        }
        if (type == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_MESSAGE_TYPE);
        }
        if (content == null || content.isBlank()) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_MESSAGE_CONTENT);
        }

        ChatMessage msg = new ChatMessage();
        msg.chatRoom = room;
        msg.sender = sender;
        msg.messageType = type;
        msg.content = content;
        msg.sessionNo = room.getCurrentSessionNo();

        return msg;
    }
}
