package com.back.matchduo.domain.chat.service;

import com.back.matchduo.domain.chat.dto.internal.ChatMessagesWithRoom;
import com.back.matchduo.domain.chat.entity.ChatMessage;
import com.back.matchduo.domain.chat.entity.ChatMessageRead;
import com.back.matchduo.domain.chat.entity.ChatRoom;
import com.back.matchduo.domain.chat.entity.MessageType;
import com.back.matchduo.domain.chat.repository.ChatMessageReadRepository;
import com.back.matchduo.domain.chat.repository.ChatMessageRepository;
import com.back.matchduo.domain.chat.repository.ChatRoomRepository;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.repository.UserRepository;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageReadRepository chatMessageReadRepository;
    private final UserRepository userRepository;

    /** 메시지 전송 */
    public ChatMessage send(Long chatRoomId, Long senderId, MessageType type, String content) {
        ChatRoom room = getRoomOrThrow(chatRoomId);

        validateSenderId(senderId);
        User sender = userRepository.getReferenceById(senderId);

        validateMember(room, senderId);
        validateRoomOpen(room);

        ChatMessage message = ChatMessage.create(room, sender, type, content);
        return chatMessageRepository.save(message);
    }

    /**
     * 메시지 목록과 채팅방 정보를 함께 조회 (중복 조회 방지)
     * - cursorMessageId가 null이면 최신부터
     * - 현재 세션(room.currentSessionNo)의 메시지만 조회
     * - 결과는 최신 -> 과거(desc) 정렬로 반환
     * */
    @Transactional(readOnly = true)
    public ChatMessagesWithRoom getMessagesWithRoom(Long chatRoomId, Long requesterId, Long cursorMessageId, int size) {
        return loadMessagesWithRoom(chatRoomId, requesterId, cursorMessageId, size);
    }

    /**
     * 공통 조회 로직 (중복 제거)
     * - room 조회/검증 + pageable 생성 + sessionNo 검증 + 메시지 조회를 한 곳에서 처리
     */
    private ChatMessagesWithRoom loadMessagesWithRoom(Long chatRoomId, Long requesterId, Long cursorMessageId, int size) {
        ChatRoom room = getRoomOrThrow(chatRoomId);
        validateMember(room, requesterId);

        int pageSize = (size <= 0 || size > 100) ? 30 : size;
        Pageable pageable = PageRequest.of(0, pageSize);

        Integer sessionNo = room.getCurrentSessionNo();
        if (sessionNo == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_SESSION);
        }

        List<ChatMessage> messages = chatMessageRepository.findByCursorWithSender(
                chatRoomId, sessionNo, cursorMessageId, pageable);

        return new ChatMessagesWithRoom(messages, room);
    }

    /**
     * 읽음 처리 (마지막 읽은 메시지 포인터)
     * - 현재 세션 메시지만 반영 (이전 세션 메시지는 무시)
     */
    public ChatMessageRead markReadUpTo(Long chatRoomId, Long requesterId, Long chatMessageId) {
        if (requesterId == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_USER_ID);
        }
        if (chatMessageId == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_MESSAGE);
        }

        ChatRoom room = getRoomOrThrow(chatRoomId);
        validateMember(room, requesterId);

        ChatMessage message = getMessageOrThrow(chatMessageId);

        // 메시지-채팅방 불일치 시 차단
        if (message.getChatRoom() == null || message.getChatRoom().getId() == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_CHAT_ROOM);
        }
        if (!room.getId().equals(message.getChatRoom().getId())) {
            throw new CustomException(CustomErrorCode.CHAT_ROOM_MISMATCH);
        }

        // (room, user) 1행, 없으면 생성
        ChatMessageRead readState = chatMessageReadRepository
                .findByChatRoomIdAndUserIdWithLock(room.getId(), requesterId)
                        .orElseGet(() -> {
                            try {
                                User user = userRepository.getReferenceById(requesterId);
                                return chatMessageReadRepository.save(ChatMessageRead.create(room, user));
                            } catch (DataIntegrityViolationException e) {
                                return chatMessageReadRepository.findByChatRoomIdAndUserId(room.getId(), requesterId)
                                        .orElseThrow(() -> new CustomException(CustomErrorCode.CHAT_READ_STATE_INVALID));

                            }
                        });

        readState.markReadUpTo(message);
        return chatMessageReadRepository.save(readState);
    }

    /** 헬퍼 메서드 */
    private ChatRoom getRoomOrThrow(Long chatRoomId) {
        if (chatRoomId == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_CHAT_ROOM);
        }
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private ChatMessage getMessageOrThrow(Long chatMessageId) {
        if (chatMessageId == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_MESSAGE);
        }
        return chatMessageRepository.findById(chatMessageId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.CHAT_MESSAGE_NOT_FOUND));
    }

    private void validateSenderId(Long senderId) {
        if (senderId == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_SENDER);
        }
    }

    private void validateMember(ChatRoom room, Long userId) {
        if (userId == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_USER_ID);
        }
        room.getMemberRole(userId); // 방 구성원 검증
    }

    private void validateRoomOpen(ChatRoom room) {
        if (room.isClosed()) { // 한쪽이라도 나가면 채팅방 닫힘
            throw new CustomException(CustomErrorCode.CHAT_ROOM_CLOSED);
        }
    }

}
