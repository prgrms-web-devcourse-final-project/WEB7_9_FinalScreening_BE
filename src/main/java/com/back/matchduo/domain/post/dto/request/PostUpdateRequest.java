package com.back.matchduo.domain.post.dto.request;

import com.back.matchduo.domain.post.entity.Position;
import com.back.matchduo.domain.post.entity.QueueType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostUpdateRequest(
        Position myPosition,

        @Size(max = 3, message = "찾는 포지션은 최대 3개까지 선택 가능합니다.")
        List<Position> lookingPositions,

        QueueType queueType,

        Boolean mic,

        @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
        @Max(value = 5, message = "최대 5명까지 가능합니다.")
        Integer recruitCount,

        String memo
) {
}
