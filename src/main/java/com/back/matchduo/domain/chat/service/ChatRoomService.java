package com.back.matchduo.domain.chat.service;

import com.back.matchduo.domain.chat.dto.internal.ChatRoomDetailWithGameAccount;
import com.back.matchduo.domain.chat.dto.response.ChatRoomSummaryResponse;
import com.back.matchduo.domain.chat.entity.ChatMessage;
import com.back.matchduo.domain.chat.entity.ChatMessageRead;
import com.back.matchduo.domain.chat.entity.ChatRoom;
import com.back.matchduo.domain.chat.repository.ChatMessageReadRepository;
import com.back.matchduo.domain.chat.repository.ChatMessageRepository;
import com.back.matchduo.domain.chat.repository.ChatRoomRepository;
import com.back.matchduo.domain.gameaccount.entity.GameAccount;
import com.back.matchduo.domain.gameaccount.repository.GameAccountRepository;
import com.back.matchduo.domain.post.entity.Post;
import com.back.matchduo.domain.post.repository.PostRepository;
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
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageReadRepository chatMessageReadRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final GameAccountRepository gameAccountRepository;

    /**
     * 채팅방 생성 (멱등)
     * - 기존 방 있으면 재사용 (닫혀있으면 새 세션으로 재활성화)
     * - 없으면 새로 생성
     */
    public ChatRoom createOrGet(Long postId, Long senderId) {
        if (postId == null) {
            throw new CustomException(CustomErrorCode.POST_ID_REQUIRED);
        }
        if (senderId == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_USER_ID);
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.POST_NOT_FOUND));

        // receiver = Post 작성자
        User receiver = post.getUser();
        if (receiver == null || receiver.getId() == null) {
            throw new CustomException(CustomErrorCode.NOT_FOUND_USER);
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));

        // 본인에게 채팅 불가
        if (receiver.getId().equals(sender.getId())) {
            throw new CustomException(CustomErrorCode.CHAT_SAME_SENDER_RECEIVER);
        }

        // 1) 기존 채팅방 조회
        return chatRoomRepository.findByPostIdAndSenderId(postId, sender.getId())
                .map(room -> {
                    if (room.isClosed()) {
                        // 닫힌 방이면 락 걸고 다시 조회후 재활성화
                        ChatRoom lockedRoom = chatRoomRepository.findByPostIdAndSenderIdWithLock(postId, sender.getId())
                                        .orElseThrow(() -> new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND));
                        if (lockedRoom.isClosed()) {
                            lockedRoom.resumeAsNewSession();
                            resetReadStates(lockedRoom, sender, receiver);
                            return chatRoomRepository.save(lockedRoom);
                        }
                        return lockedRoom;
                    }
                    return room;
                })
                .orElseGet(() -> {
                    // 2) 없으면 새 채팅방 생성 (동시성 레이스 대비)
                    try {
                        ChatRoom newRoom = ChatRoom.create(post, receiver, sender);
                        ChatRoom saved = chatRoomRepository.save(newRoom);

                        // (room, user) 당 1행: 읽음 상태 생성 (senderId, receiver 각각)
                        chatMessageReadRepository.save(ChatMessageRead.create(saved, sender));
                        chatMessageReadRepository.save(ChatMessageRead.create(saved, receiver));

                        return saved;
                    } catch (DataIntegrityViolationException e) {
                        // UNIQUE(post_id, sender_id) 충돌 등 누군가가 동시에 먼저 생성한 경우
                        ChatRoom existingRoom = chatRoomRepository.findByPostIdAndSenderId(postId, sender.getId())
                                .orElseThrow(() -> e);

                        // 닫혀있으면 재활성화
                        if (existingRoom.isClosed()) {
                            existingRoom.resumeAsNewSession();
                            resetReadStates(existingRoom, sender, receiver);
                            return chatRoomRepository.save(existingRoom);
                        }
                        return existingRoom;
                    }
                });
    }

    /**
     * 채팅방 나가기
     */
    public ChatRoom leave(Long chatRoomId, Long userId) {
        ChatRoom room = chatRoomRepository.findByIdWithLock(chatRoomId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND));
        validateMember(room, userId);

        room.leave(userId);
        return chatRoomRepository.save(room);
    }

    /**
     * 내 채팅방 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ChatRoom> getMyRooms(Long userId, Long cursorId, int size) {
        if (userId == null) throw new CustomException(CustomErrorCode.CHAT_INVALID_USER_ID);

        int pageSize = (size <= 0 || size > 50) ? 20 : size;
        Pageable pageable = PageRequest.of(0, pageSize);

        return chatRoomRepository.findMyRooms(userId, cursorId, pageable);
    }

    /**
     * 내 채팅방 목록 조회 (Summary 포함)
     */
    @Transactional(readOnly = true)
    public List<ChatRoomSummaryResponse> getMyRoomsWithSummary(Long userId, Long cursorId, int size) {
        List<ChatRoom> rooms = getMyRooms(userId, cursorId, size);

        return rooms.stream()
                .map(room -> {
                    Integer sessionNo = room.getCurrentSessionNo();
                    if (sessionNo == null) {
                        sessionNo = 1;
                    }

                    // 마지막 메시지 조회
                    ChatMessage lastMessage = chatMessageRepository
                            .findFirstByChatRoomIdAndSessionNoOrderByIdDesc(room.getId(), sessionNo)
                            .orElse(null);

                    // 안 읽은 메시지 수 계산
                    int unreadCount = 0;
                    ChatMessageRead readState = chatMessageReadRepository
                            .findByChatRoomIdAndUserId(room.getId(), userId)
                            .orElse(null);

                    if (readState != null && readState.getLastReadMessage() != null) {
                        unreadCount = (int) chatMessageRepository.countUnread(
                                room.getId(), sessionNo, readState.getLastReadMessage().getId());
                    } else if (lastMessage != null) {
                        // 읽은 메시지가 없으면 전체 메시지가 안 읽음
                        unreadCount = (int) chatMessageRepository.countUnread(room.getId(), sessionNo, 0L);
                    }

                    return ChatRoomSummaryResponse.of(room, userId, lastMessage, unreadCount);
                })
                .toList();
    }

    /** 채팅방 상세 조회 (상대방 게임 계정 정보 포함) */
    @Transactional(readOnly = true)
    public ChatRoomDetailWithGameAccount getRoomWithGameAccount(Long chatRoomId, Long userId) {
        ChatRoom room = getRoomWithDetailsOrThrow(chatRoomId);
        validateMember(room, userId);

        // 상대방 userId 구하기
        boolean isSender = room.isSender(userId);
        Long otherUserId = isSender ? room.getReceiver().getId() : room.getSender().getId();

        // 상대방 게임 계정 조회 (LOL 계정)
        GameAccount otherGameAccount = gameAccountRepository.findByUser_Id(otherUserId)
                .stream()
                .findFirst()
                .orElse(null);

        return new ChatRoomDetailWithGameAccount(room, otherGameAccount);
    }

    /**
     * 헬퍼 메서드
     */
    private ChatRoom getRoomOrThrow(Long chatRoomId) {
        if (chatRoomId == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_CHAT_ROOM);
        }
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    /** 채팅방 상세 조회 (sender, receiver, post 함께 로드) */
    private ChatRoom getRoomWithDetailsOrThrow(Long chatRoomId) {
        if (chatRoomId == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_CHAT_ROOM);
        }
        return chatRoomRepository.findByIdWithDetails(chatRoomId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private void validateMember(ChatRoom room, Long userId) {
        if (userId == null) {
            throw new CustomException(CustomErrorCode.CHAT_INVALID_USER_ID);
        }
        room.getMemberRole(userId);
    }

    /**
     * 새 세션 시작 시 읽음 상태 초기화
     * - (room, user) 읽음 row가 없으면 생성해서 정합성을 보장
     */
    private void resetReadStates(ChatRoom room, User sender, User receiver) {
        // 벌크 업데이트로 한 번에 초기화
        chatMessageReadRepository.resetAllForRoom(room.getId());

        // 없는 경우 생성 (새 채팅방인 경우)
        getOrCreateReadState(room, sender);
        getOrCreateReadState(room, receiver);
    }

    private ChatMessageRead getOrCreateReadState(ChatRoom room, User user) {
        return chatMessageReadRepository.findByChatRoomIdAndUserId(room.getId(), user.getId())
                .orElseGet(() -> {
                    try {
                        return chatMessageReadRepository.save(ChatMessageRead.create(room, user));
                    } catch (DataIntegrityViolationException e) {
                        return chatMessageReadRepository.findByChatRoomIdAndUserId(room.getId(), user.getId())
                                .orElseThrow(() -> new CustomException(CustomErrorCode.CHAT_READ_STATE_INVALID));
                    }
                });
    }
}
