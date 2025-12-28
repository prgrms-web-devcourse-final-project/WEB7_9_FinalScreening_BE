package com.back.matchduo.domain.review.eventListener;

import com.back.matchduo.domain.party.entity.Party;
import com.back.matchduo.domain.party.entity.PartyMember;
import com.back.matchduo.domain.party.entity.PartyStatus;
import com.back.matchduo.domain.party.repository.PartyMemberRepository;
import com.back.matchduo.domain.party.repository.PartyRepository;
import com.back.matchduo.domain.review.entity.ReviewRequest;
import com.back.matchduo.domain.review.enums.ReviewRequestStatus;
import com.back.matchduo.domain.review.event.PartyStatusChangedEvent;
import com.back.matchduo.domain.review.repository.ReviewRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewRequestManagementListener {

    private final ReviewRequestRepository reviewRequestRepository;
    private final PartyRepository partyRepository;
    private final PartyMemberRepository partyMemberRepository;

    @Async("eventTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePartyStatusChange(PartyStatusChangedEvent event) {
        Long partyId = event.getPartyId();

        // 모집 완료 (ACTIVE) -> 리뷰 요청서 미리 생성 (상태: PENDING)
        if (event.getNewStatus() == PartyStatus.ACTIVE) {
            createReviewRequests(partyId);
        }
        
        // 다시 모집 중 (RECRUIT) -> 기존 요청서 삭제
        else if (event.getNewStatus() == PartyStatus.RECRUIT) {
            deleteReviewRequests(partyId);
        }

        // CLOSED (게임 완료/파티 종료) -> 리뷰 작성 가능하도록 활성화 (상태 변경)
        else if (event.getNewStatus() == PartyStatus.CLOSED) {
            activateReviewRequests(partyId);
        }
    }

    private void createReviewRequests(Long partyId) {

        if (reviewRequestRepository.existsByPartyId(partyId)) return;

        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new EntityNotFoundException("파티를 찾을 수 없습니다."));

        // 파티원 조회
        List<PartyMember> members = partyMemberRepository.findAllByPartyId(partyId);

        List<ReviewRequest> requests = members.stream()
            .map(member -> ReviewRequest.builder()
                .party(party)
                .requestUser(member.getUser())
                .build())
            .toList();
            
        reviewRequestRepository.saveAll(requests);
    }

    private void deleteReviewRequests(Long partyId) {
        // 이미 완료된(COMPLETED) 건은 지우면 안 됨
        reviewRequestRepository.deleteByPartyIdAndStatus(partyId, ReviewRequestStatus.PENDING);
    }

    private void activateReviewRequests(Long partyId) {
        // 파티 종료 시 PENDING -> COMPLETED로 바꿔서 유저가 리뷰를 쓸 수 있게 함
        List<ReviewRequest> requests = reviewRequestRepository.findAllByPartyIdAndStatus(partyId, ReviewRequestStatus.PENDING);
        requests.forEach(ReviewRequest::complete);
    }
}