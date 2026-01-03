package com.back.matchduo.domain.chat.service;

import com.back.matchduo.domain.chat.dto.response.ChatRoomSummaryResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomListCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CHAT_LIST_KEY_PREFIX = "chat:list:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private String buildKey(Long userId) {
        return CHAT_LIST_KEY_PREFIX + userId;
    }

    /**
     * 채팅방 목록 캐시 조회
     */
    public List<ChatRoomSummaryResponse> get(Long userId) {
        try {
            String key = buildKey(userId);
            String json = stringRedisTemplate.opsForValue().get(key);

            if (json == null) {
                return null;
            }

            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Redis 채팅방 목록 조회 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 채팅방 목록 캐시 저장
     */
    public void set(Long userId, List<ChatRoomSummaryResponse> chatRooms) {
        try {
            String key = buildKey(userId);
            String json = objectMapper.writeValueAsString(chatRooms);
            stringRedisTemplate.opsForValue().set(key, json, TTL);
            log.debug("Redis 채팅방 목록 저장: userId={}, count={}", userId, chatRooms.size());
        } catch (Exception e) {
            log.warn("Redis 채팅방 목록 저장 실패: {}", e.getMessage());
        }
    }

    /**
     * 특정 사용자의 채팅방 목록 캐시 삭제
     */
    public void evict(Long userId) {
        try {
            stringRedisTemplate.delete(buildKey(userId));
            log.debug("Redis 채팅방 목록 삭제: userId={}", userId);
        } catch (Exception e) {
            log.warn("Redis 채팅방 목록 삭제 실패: {}", e.getMessage());
        }
    }

    /**
     * 채팅방의 양쪽 사용자 캐시 모두 삭제
     */
    public void evictBoth(Long userId1, Long userId2) {
        evict(userId1);
        evict(userId2);
    }
}