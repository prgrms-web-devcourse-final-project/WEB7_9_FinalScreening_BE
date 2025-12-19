package com.back.matchduo.domain.chat.controller;

import com.back.matchduo.domain.chat.dto.internal.ChatMessagesWithRoom;
import com.back.matchduo.domain.chat.dto.internal.ChatRoomDetailWithGameAccount;
import com.back.matchduo.domain.chat.dto.request.ChatMessageReadRequest;
import com.back.matchduo.domain.chat.dto.request.ChatMessageSendRequest;
import com.back.matchduo.domain.chat.dto.request.ChatRoomCreateRequest;
import com.back.matchduo.domain.chat.dto.response.*;
import com.back.matchduo.domain.chat.entity.ChatMessage;
import com.back.matchduo.domain.chat.entity.ChatMessageRead;
import com.back.matchduo.domain.chat.entity.ChatRoom;
import com.back.matchduo.domain.chat.service.ChatMessageService;
import com.back.matchduo.domain.chat.service.ChatRoomService;
import com.back.matchduo.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequiredArgsConstructor
@Tag(name = "Chat", description = "채팅 API")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    @Operation(summary = "채팅방 생성", description = "모집글 기반 1:1 채팅방을 생성합니다. 이미 존재하면 기존 채팅방을 반환합니다 (멱등성 보장).")
    @PostMapping("/api/v1/chats")
    public ResponseEntity<ChatRoomCreateResponse> createOrGetRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChatRoomCreateRequest request) {

        Long userId = userDetails.getId();
        ChatRoom room = chatRoomService.createOrGet(request.postId(), userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ChatRoomCreateResponse.of(room, userId));
    }

    @Operation(summary = "내 채팅방 목록 조회", description = "로그인한 사용자가 참여 중인 채팅방 목록을 조회합니다. 커서 기반 페이징을 지원합니다.")
    @GetMapping("/api/v1/chats")
    public ResponseEntity<ChatRoomListResponse> getMyRooms(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = userDetails.getId();
        int pageSize = Math.min(Math.max(size, 1), 100);
        List<ChatRoomSummaryResponse> summaries = chatRoomService.getMyRoomsWithSummary(userId, cursor, pageSize + 1);

        boolean hasNext = summaries.size() > pageSize;
        if (hasNext) {
            summaries = summaries.subList(0, pageSize);
        }
        String nextCursor = hasNext && !summaries.isEmpty()
                ? String.valueOf(summaries.get(summaries.size() - 1).chatRoomId())
                : null;

        return ResponseEntity.ok(new ChatRoomListResponse(
                summaries,
                nextCursor,
                hasNext
        ));
    }

    @Operation(summary = "채팅방 상세 조회", description = "채팅방의 상세 정보를 조회합니다. 상대방 정보, 모집글 요약, 게임 계정 정보를 포함합니다.")
    @GetMapping("/api/v1/chats/{chatId}")
    public ResponseEntity<ChatRoomDetailResponse> getRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long chatId) {

        Long userId = userDetails.getId();
        ChatRoomDetailWithGameAccount result = chatRoomService.getRoomWithGameAccount(chatId, userId);
        return ResponseEntity.ok(ChatRoomDetailResponse.of(result.room(), userId, result.otherGameAccount()));
    }

    @Operation(summary = "채팅방 나가기", description = "채팅방에서 퇴장합니다. 한 명이라도 나가면 해당 채팅방은 닫히며 메시지 전송이 불가합니다.")
    @DeleteMapping("/api/v1/chats/{chatId}")
    public ResponseEntity<ChatRoomLeaveResponse> leaveRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long chatId) {

        Long userId = userDetails.getId();
        ChatRoom room = chatRoomService.leave(chatId, userId);
        return ResponseEntity.ok(ChatRoomLeaveResponse.of(room));
    }

    @Operation(summary = "메시지 전송", description = "채팅방에 메시지를 전송합니다. TEXT, SYSTEM 타입을 지원합니다.")
    @PostMapping("/api/v1/chats/{chatId}/messages")
    public ResponseEntity<ChatMessageSendResponse> sendMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long chatId,
            @Valid @RequestBody ChatMessageSendRequest request) {

        Long userId = userDetails.getId();
        ChatMessage message = chatMessageService.send(
                chatId, userId, request.messageType(), request.content());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ChatMessageSendResponse.of(message));
    }

    @Operation(summary = "메시지 목록 조회", description = "채팅방의 메시지 목록을 조회합니다. 최신순 정렬, 커서 기반 페이징을 지원합니다.")
    @GetMapping("/api/v1/chats/{chatId}/messages")
    public ResponseEntity<ChatMessageListResponse> getMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long chatId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "30") int size) {

        Long userId = userDetails.getId();
        int pageSize = Math.min(Math.max(size, 1), 100);

        ChatMessagesWithRoom result =
                chatMessageService.getMessagesWithRoom(chatId, userId, cursor, pageSize + 1);

        List<ChatMessage> messages = result.messages();
        boolean hasNext = messages.size() > pageSize;

        List<ChatMessage> finalMessages = hasNext
                ? new ArrayList<>(messages.subList(0, pageSize))
                : new ArrayList<>(messages);

        Long nextCursor = hasNext && !finalMessages.isEmpty()
                ? finalMessages.get(finalMessages.size() - 1).getId()
                : null;

        return ResponseEntity.ok(ChatMessageListResponse.of(
                result.room(),
                userId,
                finalMessages,
                nextCursor,
                hasNext
        ));
    }

    @Operation(summary = "메시지 읽음 처리", description = "지정한 메시지까지 읽음 처리합니다. 안 읽은 메시지 수 계산에 사용됩니다.")
    @PostMapping("/api/v1/chats/{chatId}/messages/read")
    public ResponseEntity<ChatMessageReadResponse> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long chatId,
            @Valid @RequestBody ChatMessageReadRequest request) {

        Long userId = userDetails.getId();
        ChatMessageRead readState = chatMessageService.markReadUpTo(
                chatId, userId, request.lastReadMessageId());

        return ResponseEntity.ok(ChatMessageReadResponse.of(
                chatId,
                readState.getLastReadMessage() != null ? readState.getLastReadMessage().getId() : null,
                readState.getLastReadAt()
        ));
    }
}