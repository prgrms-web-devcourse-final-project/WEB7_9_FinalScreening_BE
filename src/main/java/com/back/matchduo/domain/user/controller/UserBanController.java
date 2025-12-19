package com.back.matchduo.domain.user.controller;

import com.back.matchduo.domain.user.dto.response.UserBlockListResponse;
import com.back.matchduo.domain.user.service.UserBanService;
import com.back.matchduo.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserBanController {

    private final UserBanService userBanService;

    //유저 차단 등록
    @PostMapping("/{userId}/blocks")
    public ResponseEntity<Void> blockUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userBanService.blockUser(userDetails.getId(), userId);
        return ResponseEntity.ok().build();
    }

    //차단 목록 조회
    @GetMapping("/me/blocks")
    public ResponseEntity<List<UserBlockListResponse>> getMyBlockList(
            @AuthenticationPrincipal
            CustomUserDetails userDetails) {
        return ResponseEntity.ok(userBanService.userBlockListResponses(userDetails.getId()));
    }

    //차단 해제
    @DeleteMapping("/me/blocks/{targetUserId}")
    public ResponseEntity<Void> unblockUser(
            @PathVariable
            Long targetUserId,

            @AuthenticationPrincipal
            CustomUserDetails userDetails) {
        userBanService.unblockUser(userDetails.getId(), targetUserId);
        return ResponseEntity.ok().build();
    }
}