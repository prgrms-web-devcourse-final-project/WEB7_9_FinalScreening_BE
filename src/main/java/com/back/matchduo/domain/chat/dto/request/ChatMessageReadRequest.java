package com.back.matchduo.domain.chat.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChatMessageReadRequest(
        @NotNull Long lastReadMessageId
) {}
