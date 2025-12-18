package com.back.matchduo.domain.chat.ws;

import com.back.matchduo.domain.chat.dto.request.ChatMessageSendRequest;
import com.back.matchduo.domain.chat.dto.response.ChatMessageSendResponse;
import com.back.matchduo.domain.chat.entity.ChatMessage;
import com.back.matchduo.domain.chat.service.ChatMessageService;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket 채팅 메시지
 * - /pub/chats/{chatId}/messages -> 메시지 수신 후 /sub/chats/{chatId}로 브로드캐스트
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    /** WebSocket으로 메시지 전송 및 브로드캐스트 */
    @MessageMapping("/chats/{chatId}/messages")
    public void sendMessage(
            @DestinationVariable Long chatId,
            @Payload ChatMessageSendRequest request,
            Principal principal
    ) {
        Long userId = extractUserId(principal);

        // 메시지 저장
        ChatMessage message = chatMessageService.send(
                chatId, userId, request.messageType(), request.content());

        ChatMessageSendResponse response = ChatMessageSendResponse.of(message);

        // 해당 채팅방 구독자들에게 브로드캐스트
        messagingTemplate.convertAndSend("/sub/chats/" + chatId, response);

        log.debug("WebSocket 메시지 전송: chatId={}, senderId={}", chatId, userId);
    }

    private Long extractUserId(Principal principal) {
        if (principal == null) {
            throw new CustomException(CustomErrorCode.UNAUTHORIZED_USER);
        }

        if (principal instanceof Authentication auth) {
            return (Long) auth.getPrincipal();
        }

        throw new CustomException(CustomErrorCode.UNAUTHORIZED_USER);
    }
}
