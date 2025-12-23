package com.back.matchduo.domain.user.controller;

import com.back.matchduo.domain.user.dto.request.UserProfileRequest;
import com.back.matchduo.domain.user.dto.request.UserUpdateRequest;
import com.back.matchduo.domain.user.dto.response.UserProfileResponse;
import com.back.matchduo.domain.user.service.UserProfileService;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import com.back.matchduo.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me")
public class UserProfileController {
    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserProfileResponse response = userProfileService.getProfile(userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    //닉네임만 수정
    @PatchMapping("/nickname")
    public ResponseEntity<Void> updateNickname(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request) {
        userProfileService.updateNickname(userDetails.getUser(), request.nickname());
        return ResponseEntity.ok().build();
    }

    //자기소개만 수정
    @PatchMapping("/comment")
    public ResponseEntity<Void> updateComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request) {
        userProfileService.updateComment(userDetails.getUser(), request.comment());
        return ResponseEntity.ok().build();
    }

    //비밀번호만 수정
    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserUpdateRequest request) {
        userProfileService.updatePassword(userDetails.getUser(), request);
        return ResponseEntity.ok().build();
    }

    //프로필 이미지만 수정
    @PutMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateProfileWithFile(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @RequestPart(value = "file", required = false)
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(CustomErrorCode.INVALID_REQUEST); // 파일이 없으면 에러
        }

        userProfileService.updateProfileImage(userDetails.getUser(), file);
        return ResponseEntity.ok().build();
    }
}