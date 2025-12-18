package com.back.matchduo.domain.post.dto.request;

import com.back.matchduo.domain.post.entity.Position;
import com.back.matchduo.domain.post.entity.QueueType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

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

        @Size(max = 50, message = "모집 내용은 1~50자여야 합니다.")
        @Pattern(regexp = "^(?!\\s*$).+", message = "모집 내용은 공백만 입력할 수 없습니다.")
        String memo
) {
}
