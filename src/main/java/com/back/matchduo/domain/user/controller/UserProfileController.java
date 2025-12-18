package com.back.matchduo.domain.user.controller;

import com.back.matchduo.domain.user.dto.request.UserProfileRequest;
import com.back.matchduo.domain.user.dto.request.UserUpdateRequest;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.service.UserProfileService;
import com.back.matchduo.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me")
public class UserProfileController {
    private final UserProfileService userProfileService;

    //프로필 조회
    @GetMapping
    public ResponseEntity<UserProfileRequest> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserProfileRequest response = userProfileService.getProfile(userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    //프로필 수정
    @PutMapping
    public ResponseEntity<Void> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,

            @Valid
            @RequestBody
            UserUpdateRequest request
    ) {
        userProfileService.updateProfile(userDetails.getUser(), request);
        return ResponseEntity.ok().build();
    }

    // 프로필 이미지 포함 수정
    @PutMapping("/all")
    public ResponseEntity<Void> updateProfileWithFile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestPart(required = false) UserUpdateRequest request,
            @RequestPart(required = false) MultipartFile file // 추가할 부분
    ) {
        userProfileService.updateProfileWithFile(userDetails.getUser(), request, file); // 추가할 부분
        return ResponseEntity.ok().build();
    }
}
