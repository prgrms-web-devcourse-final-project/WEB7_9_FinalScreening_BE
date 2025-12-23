package com.back.matchduo.global.security.websocket;

import com.back.matchduo.domain.chat.repository.ChatRoomRepository;
import com.back.matchduo.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WebSocket STOMP 인증/인가
 * - CONNECT: JWT 토큰 검증 + 만료시간 저장
 * - SUBSCRIBE: 채팅방 멤버 검증
 * - SEND: 토큰 만료 여부 재검증
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final ChatRoomRepository chatRoomRepository;

    private static final Pattern CHAT_ROOM_PATTERN = Pattern.compile("/sub/chats/(\\d+)");

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        // CONNECT: JWT 인증 + 만료시간 저장
        if (StompCommand.CONNECT.equals(command)) {
            authenticateConnection(accessor);
        }

        // SUBSCRIBE: 채팅방 멤버 검증
        if (StompCommand.SUBSCRIBE.equals(command)) {
            validateSubscription(accessor);
        }

        // SEND: 토큰 만료 여부 재검증
        if (StompCommand.SEND.equals(command)) {
            validateTokenNotExpired(accessor);
        }

        return message;
    }

    private void authenticateConnection(StompHeaderAccessor accessor) {
        String token = accessor.getFirstNativeHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("WebSocket 인증 실패: 토큰 없음");
            throw new MessageDeliveryException("인증 토큰이 필요합니다.");
        }

        token = token.substring(7);

        try {
            jwtProvider.validate(token);
            Long userId = jwtProvider.getUserId(token);

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            accessor.setUser(auth);

            // 만료 시간만 세션에 저장
            Long expirationTime = jwtProvider.getExpiration(token);
            accessor.getSessionAttributes().put("tokenExpiration", expirationTime);

            log.debug("WebSocket 인증 성공: userId={}", userId);
        } catch (Exception e) {
            log.warn("WebSocket 인증 실패: {}", e.getMessage());
            throw new MessageDeliveryException("인증 실패: " + e.getMessage());
        }
    }

    private void validateSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }

        // 채팅방 구독인 경우만 검증
        Matcher matcher = CHAT_ROOM_PATTERN.matcher(destination);
        if (!matcher.matches()) {
            return;
        }

        // 인증 확인
        Authentication auth = (Authentication) accessor.getUser();
        if (auth == null) {
            log.warn("WebSocket 구독 실패: 인증 정보 없음");
            throw new MessageDeliveryException("인증이 필요합니다.");
        }

        Long userId = (Long) auth.getPrincipal();
        Long chatRoomId = Long.parseLong(matcher.group(1));

        // 채팅방 멤버 검증
        boolean isMember = chatRoomRepository.existsByIdAndMember(chatRoomId, userId);
        if (!isMember) {
            log.warn("WebSocket 구독 실패: 채팅방 멤버 아님 - chatRoomId={}, userId={}", chatRoomId, userId);
            throw new MessageDeliveryException("채팅방에 접근 권한이 없습니다.");
        }

        log.debug("WebSocket 구독 성공: chatRoomId={}, userId={}", chatRoomId, userId);
    }

    private void validateTokenNotExpired(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            throw new MessageDeliveryException("세션 정보가 없습니다.");
        }

        Long expiration = (Long) sessionAttributes.get("tokenExpiration");
        if (expiration == null || System.currentTimeMillis() > expiration) {
            log.warn("WebSocket 토큰 만료");
            throw new MessageDeliveryException("토큰이 만료되었습니다. 다시 연결해주세요.");
        }

    }
}
