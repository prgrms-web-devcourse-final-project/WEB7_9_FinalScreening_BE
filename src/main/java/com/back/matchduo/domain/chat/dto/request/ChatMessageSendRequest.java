package com.back.matchduo.domain.chat.dto.request;

import com.back.matchduo.domain.chat.entity.MessageType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChatMessageSendRequest(
        @NotNull MessageType messageType,
        @NotNull @Size(max = 2000) String content
) {}
