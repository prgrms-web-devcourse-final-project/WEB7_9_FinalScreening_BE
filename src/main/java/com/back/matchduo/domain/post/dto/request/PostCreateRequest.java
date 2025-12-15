package com.back.matchduo.domain.post.dto.request;

import com.back.matchduo.domain.post.entity.Position;
import com.back.matchduo.domain.post.entity.QueueType;
import jakarta.validation.constraints.*;

import java.util.List;

public record PostCreateRequest(
        @NotNull(message = "게임 모드를 선택해주세요.")
        Long gameModeId,

        @NotNull(message = "큐 타입을 선택해주세요.")
        QueueType queueType,

        @NotNull(message = "내 포지션을 선택해주세요.")
        Position myPosition,

        @NotEmpty(message = "찾는 포지션을 최소 1개 선택해주세요.")
        @Size(max = 3, message = "찾는 포지션은 최대 3개까지 선택 가능합니다.")
        List<Position> lookingPositions,

        @NotNull(message = "마이크 사용 여부를 선택해주세요.")
        Boolean mic,

        @NotNull(message = "모집 인원을 입력해주세요.")
        @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
        @Max(value = 5, message = "최대 5명까지 가능합니다.")
        Integer recruitCount,

        String memo
) {
}
