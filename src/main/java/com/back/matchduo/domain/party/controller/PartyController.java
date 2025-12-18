package com.back.matchduo.domain.party.controller;

import com.back.matchduo.domain.party.dto.request.PartyMemberAddRequest;
import com.back.matchduo.domain.party.dto.response.*;
import com.back.matchduo.domain.party.service.PartyService;
import com.back.matchduo.global.dto.ApiResponse;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import com.back.matchduo.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


    // 2. 파티원 초대
    @PostMapping("/parties/{partyId}/members")
    public ResponseEntity<List<PartyMemberAddResponse>> addPartyMember(
            @PathVariable Long partyId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PartyMemberAddRequest request
    ) {
        Long currentUserId = userDetails.getId();

        List<PartyMemberAddResponse> response = partyService.addMembers(partyId, currentUserId, request);
        return ResponseEntity.ok(response);
    }


    // 3. 파티원 제외 (강퇴)
    @DeleteMapping("/parties/{partyId}/members/{memberId}")
    public ResponseEntity<ApiResponse<PartyMemberRemoveResponse>> removePartyMember(
            @PathVariable Long partyId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new CustomException(CustomErrorCode.UNAUTHORIZED_USER);
        }

        Long currentUserId = userDetails.getId();

        PartyMemberRemoveResponse response = partyService.removeMember(partyId, memberId, currentUserId);

        return ResponseEntity.ok(ApiResponse.ok("파티원이 제외되었습니다.", response));    }

    // 4. 파티원 목록 조회
    @GetMapping("/parties/{partyId}/members")
    public ResponseEntity<ApiResponse<PartyMemberListResponse>> getPartyMemberList(
            @PathVariable Long partyId
    ) {
        PartyMemberListResponse response = partyService.getPartyMemberList(partyId);
        return ResponseEntity.ok(ApiResponse.ok("파티원 목록을 조회했습니다.", response));
    }


    // 5. 내가 참여한 파티 목록 조회
    @GetMapping("users/me/parties")
    public ResponseEntity<ApiResponse<MyPartyListResponse>> getMyPartyList(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new CustomException(CustomErrorCode.UNAUTHORIZED_USER);
        }

        MyPartyListResponse response = partyService.getMyPartyList(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.ok("참여한 파티 목록을 조회했습니다.", response));
    }

    // 6. 파티 상태 수동 종료(파티장)
    @PatchMapping("/parties/{partyId}/close")
    public ResponseEntity<ApiResponse<PartyCloseResponse>> closeParty(
            @PathVariable Long partyId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new CustomException(CustomErrorCode.UNAUTHORIZED_USER);
        }

        PartyCloseResponse response = partyService.closeParty(partyId, userDetails.getId());

        return ResponseEntity.ok(ApiResponse.ok("파티가 종료되었습니다.", response));
    }
}
