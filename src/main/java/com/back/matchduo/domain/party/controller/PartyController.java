package com.back.matchduo.domain.party.controller;

import com.back.matchduo.domain.party.dto.response.PartyByPostResponse;
import com.back.matchduo.domain.party.service.PartyService;
import com.back.matchduo.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Parties", description = "파티 관련 API")
public class PartyController {

    private final PartyService partyService;


    // 1. 모집글 기준 파티 상세 정보 조회
    @GetMapping("/posts/{postId}/party")
    @Operation(summary = "파티 상세 조회", description = "모집글의 파티 정보를 조회합니다.")
    public ResponseEntity<PartyByPostResponse> getPartyByPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
            // isJoined 알기 위한
    ) {
        Long currentUserId = null;
        if (userDetails != null) {
            currentUserId = userDetails.getId();
        }

        PartyByPostResponse response = partyService.getPartyByPostId(postId, currentUserId);
        return ResponseEntity.ok(response);
    }
}
