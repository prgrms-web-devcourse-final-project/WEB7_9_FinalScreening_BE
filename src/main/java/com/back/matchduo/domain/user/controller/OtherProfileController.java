package com.back.matchduo.domain.user.controller;

import com.back.matchduo.domain.user.dto.response.OtherProfileResponse;
import com.back.matchduo.domain.user.service.OtherProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Profile", description = "유저 프로필 관련 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class OtherProfileController {

    private final OtherProfileService otherProfileService;

    @Operation(summary = "타인 프로필 조회", description = "userId를 통해 다른 유저의 공개 프로필 정보를 조회합니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<OtherProfileResponse> getOtherUserProfile(@PathVariable Long userId) {
        OtherProfileResponse response = otherProfileService.getOtherUserProfile(userId);
        return ResponseEntity.ok(response);
    }
}