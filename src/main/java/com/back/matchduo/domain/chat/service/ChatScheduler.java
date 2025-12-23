package com.back.matchduo.domain.chat.service;


import com.back.matchduo.domain.chat.repository.ChatMessageReadRepository;
import com.back.matchduo.domain.chat.repository.ChatMessageRepository;
import com.back.matchduo.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatScheduler {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageReadRepository chatMessageReadRepository;
    private final ChatUnreadCacheService chatUnreadCacheService;

    /**
     * 닫힌 채팅방 정리 (매일 새벽 3시)
     *  - 양쪽 모두 나간 지 3일 이상된 채팅방 삭제
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupClosedChatRooms() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(3);

        List<Long> roomIds = chatRoomRepository.findFullyClosedIdsBefore(threshold);

        if (roomIds.isEmpty()) {
            log.debug("정리 대상 채팅방 없음");
            return;
        }

        log.info("닫힌 채팅방 {}개 정리 시작", roomIds.size());

        // Redis 캐시 삭제
        roomIds.forEach(chatUnreadCacheService::deleteByChatRoomId);

        // FK 제약 때문에 자식 먼저 삭제
        chatMessageReadRepository.deleteByRoomIds(roomIds);
        chatMessageRepository.deleteByRoomIds(roomIds);
        chatRoomRepository.deleteAllById(roomIds);

        log.info("닫힌 채팅방 {}개 정리 완료", roomIds.size());
    }

    /**
     * 오래된 메시지 정리 (매일 새벽 4시)
     * - 7일 이상된 메시지 삭제
     */
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void cleanupOldMessages() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        int deletedCount = chatMessageRepository.deleteOldMessages(threshold);

        log.info("7일 이상 된 메시지 {}개 정리 완료", deletedCount);
    }
}
