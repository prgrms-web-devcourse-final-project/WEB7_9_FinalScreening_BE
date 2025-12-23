package com.back.matchduo.domain.post.service;

import com.back.matchduo.domain.post.dto.request.PostCreateRequest;
import com.back.matchduo.domain.post.dto.request.PostUpdateRequest;
import com.back.matchduo.domain.post.entity.Position;
import com.back.matchduo.domain.post.entity.Post;
import com.back.matchduo.domain.post.entity.PostStatus;
import com.back.matchduo.domain.post.entity.QueueType;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PostValidator {

    // 모집글 작성자 검증
    public void validatePostOwner(Post post, Long userId) {
        if (post.getUser() == null || post.getUser().getId() == null || !post.getUser().getId().equals(userId)) {
            throw new CustomException(CustomErrorCode.POST_FORBIDDEN);
        }
    }

    // 생성 요청 검증
    public void validateCreate(PostCreateRequest request) {
        validateRecruitCount(request.queueType(), request.recruitCount());
        validateLookingPositions(request.lookingPositions());
        validateMemoRequired(request.memo());
    }

    // 수정 요청 검증 + 합성 검증
    public void validateUpdateMerged(Post post, PostUpdateRequest request) {
        // queueType/recruitCount 합성 검증 (핵심 3개 중 ②)
        if (request.queueType() != null || request.recruitCount() != null) {
            QueueType mergedQueueType = (request.queueType() != null) ? request.queueType() : post.getQueueType();
            Integer mergedRecruitCount = (request.recruitCount() != null) ? request.recruitCount() : post.getRecruitCount();
            validateRecruitCount(mergedQueueType, mergedRecruitCount);
        }

        if (request.lookingPositions() != null) {
            validateLookingPositions(request.lookingPositions());
        }

        if (request.memo() != null) {
            validateMemoOptional(request.memo());
        }
    }

    // 상태 변경 제한: 클라이언트는 FINISHED만 요청 가능
    public void validateStatusUpdateAllowed(PostStatus status) {
        if (status != PostStatus.CLOSED) {
            throw new CustomException(CustomErrorCode.INVALID_POST_STATUS_UPDATE);
        }
    }

    // lookingPositions 규칙 (ANY 단독)
    public void validateLookingPositions(List<Position> positions) {
        if (positions == null || positions.isEmpty()) {
            throw new CustomException(CustomErrorCode.INVALID_LOOKING_POSITIONS);
        }

        Set<Position> unique = new HashSet<>(positions);

        // ANY 선택 시 ANY 단독만 허용
        if (unique.contains(Position.ANY)) {
            if (unique.size() != 1) {
                throw new CustomException(CustomErrorCode.INVALID_LOOKING_POSITIONS);
            }
        }

        // 최대 3개 제한은 DTO @Size로 1차 검증되지만, 중복 제거 후에도 체크
        if (unique.size() > 3) {
            throw new CustomException(CustomErrorCode.INVALID_LOOKING_POSITIONS);
        }
    }

    // memo: 필수
    private void validateMemoRequired(String memo) {
        if (memo == null) {
            throw new CustomException(CustomErrorCode.INVALID_POST_MEMO);
        }
        String trimmed = memo.trim();
        if (trimmed.isEmpty() || trimmed.length() < 1 || trimmed.length() > 50) {
            throw new CustomException(CustomErrorCode.INVALID_POST_MEMO);
        }
    }

    // memo 수정: 포함된 경우만 검증
    private void validateMemoOptional(String memo) {
        String trimmed = memo.trim();
        if (trimmed.isEmpty() || trimmed.length() < 1 || trimmed.length() > 50) {
            throw new CustomException(CustomErrorCode.INVALID_POST_MEMO);
        }
    }

    // 큐 타입별 모집 인원 검증
    private void validateRecruitCount(QueueType queueType, Integer recruitCount) {
        if (queueType == null || recruitCount == null) {
            throw new CustomException(CustomErrorCode.INVALID_RECRUIT_COUNT);
        }

        switch (queueType) {
            case DUO -> {
                if (recruitCount != 2) {
                    throw new CustomException(CustomErrorCode.INVALID_RECRUIT_COUNT);
                }
            }
            case FLEX -> {
                if (recruitCount != 2 && recruitCount != 3 && recruitCount != 5) {
                    throw new CustomException(CustomErrorCode.INVALID_RECRUIT_COUNT);
                }
            }
            case NORMAL -> {
                if (recruitCount < 2 || recruitCount > 5) {
                    throw new CustomException(CustomErrorCode.INVALID_RECRUIT_COUNT);
                }
            }
        }
    }
}
