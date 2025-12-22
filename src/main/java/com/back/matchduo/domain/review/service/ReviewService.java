package com.back.matchduo.domain.review.service;

import com.back.matchduo.domain.party.repository.PartyMemberRepository;
import com.back.matchduo.domain.review.dto.request.ReviewCreateRequest;
import com.back.matchduo.domain.review.dto.request.ReviewUpdateRequest;
import com.back.matchduo.domain.review.dto.response.*;
import com.back.matchduo.domain.review.entity.Review;
import com.back.matchduo.domain.review.entity.ReviewRequest;
import com.back.matchduo.domain.review.enums.ReviewEmoji;
import com.back.matchduo.domain.review.enums.ReviewRequestStatus;
import com.back.matchduo.domain.review.repository.ReviewRepository;
import com.back.matchduo.domain.review.repository.ReviewRequestRepository;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewRequestRepository reviewRequestRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final UserRepository userRepository;

    // 리뷰 작성
    public ReviewCreateResponse createReview(Long currentUserId, ReviewCreateRequest reqDto) {
        Long postId = reqDto.postId();
        Long revieweeId = reqDto.revieweeId();

        // 요청서 조회
        ReviewRequest reviewRequest = reviewRequestRepository.findByPostIdAndRequestUserId(postId, currentUserId).
                orElseThrow(() -> new IllegalArgumentException("참여하지 않은 파티입니다."));

        // 리뷰요청관리 상태 검증 : COMPLETED인가
        if(reviewRequest.getStatus() != ReviewRequestStatus.COMPLETED) {
            throw new IllegalStateException("아직 게임이 종료되지 않아 리뷰를 작성할 수 없습니다.");
        }

        // 중복 작성 검증
        if(reviewRepository.existsByPostIdAndReviewerIdAndRevieweeId(postId,currentUserId,revieweeId)) {
            throw new IllegalStateException("이미 작성한 리뷰입니다.");
        }

        // 파티원 여부 검증
        if(!partyMemberRepository.existsByPartyIdAndUserId(postId,revieweeId)) {
            throw new IllegalArgumentException("파티원이 아닌 대상입니다.");
        }

        User reviewer = reviewRequest.getRequestUser();
        User reviewee = userRepository.findById(revieweeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Review review = Review.builder()
                .post(reviewRequest.getPost())
                .reviewer(reviewer)
                .reviewee(reviewee)
                .reviewRequest(reviewRequest)
                .emoji(reqDto.emoji())
                .content(reqDto.content())
                .build();

        Review savedReview = reviewRepository.save(review);

        long totalTeamMembers = partyMemberRepository.countByParty_PostId(postId) - 1;
        long myReviewCount = reviewRepository.countByPostIdAndReviewerId(postId, currentUserId);

        if (myReviewCount == totalTeamMembers) reviewRequest.deactivate();

        return ReviewCreateResponse.from(savedReview);
    }

    // 리뷰 수정
    public ReviewUpdateResponse updateReview(Long reviewId, Long userId, ReviewUpdateRequest reqDto) {
        Review review = reviewRepository.findByIdAndReviewerId(reviewId, userId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없거나 수정 권한이 없습니다."));

        review.update(reqDto.emoji(), reqDto.content());

        return ReviewUpdateResponse.from(review);
    }

    // 리뷰 삭제
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findByIdAndReviewerId(reviewId, userId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없거나 삭제 권한이 없습니다."));

        review.deactivate();
    }

    // 특정 유저가 받은 리뷰목록 조회
    public List<ReviewListResponse> getReviewsReceivedByUser(Long userId) {
        List<Review> reviews = reviewRepository.findAllByRevieweeId(userId);

        return reviews.stream()
                .map(ReviewListResponse::from)
                .toList();
    }

    // 내가 작성한 리뷰 목록 조회
    public List<MyReviewListResponse> getMyWrittenReviews(Long userId) {
        List<Review> reviews = reviewRepository.findAllByReviewerId(userId);

        return reviews.stream()
                .map(MyReviewListResponse::from)
                .toList();
    }

    // 특정 유저 리뷰 분포 조회(비율)
    public ReviewDistributionResponse getReviewDistribution(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        List<Object[]> results = reviewRepository.countReviewEmojisByRevieweeId(userId);

        long goodCount = 0;
        long normalCount = 0;
        long badCount = 0;

        for (Object[] result : results) {
            ReviewEmoji emoji = (ReviewEmoji) result[0];
            Long count = (Long) result[1];

            switch (emoji) {
                case GOOD -> goodCount = count;
                case NORMAL -> normalCount = count;
                case BAD -> badCount = count;
            }
        }

        return ReviewDistributionResponse.of(userId,user.getNickname(),goodCount, normalCount, badCount);
    }
}
