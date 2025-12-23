package com.back.matchduo.domain.party.service;


import com.back.matchduo.domain.party.entity.Party;
import com.back.matchduo.domain.party.entity.PartyStatus;
import com.back.matchduo.domain.party.repository.PartyRepository;
import com.back.matchduo.domain.post.entity.PostStatus;
import com.back.matchduo.domain.post.repository.PostRepository;
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
public class PartyScheduler {
    private final PartyRepository partyRepository;
    private final PostRepository postRepository;

    @Scheduled(cron = "0 * * * * *") // 매 분 0초마다 실행 (1분 주기)
    @Transactional
    public void autoCloseExpiredParties() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 만료된 파티 조회 (Repository 메서드 활용)
        List<Party> expiredParties = partyRepository.findByStatusAndExpiresAtBefore(PartyStatus.ACTIVE, now);
        if (expiredParties.isEmpty()) {
            return;
        }

        log.info("자동 종료 대상 파티 {}개를 발견하여 종료 처리합니다.", expiredParties.size());

        // 2. 파티 종료 처리 (Entity 메서드 활용)
        for (Party party : expiredParties){
            party.expireParty(); // 상태를 CLOSED로 변경, closedAt 설정

        postRepository.findById(party.getPostId())
                .ifPresent(post -> post.updateStatus(PostStatus.CLOSED));
        }
    }
}
