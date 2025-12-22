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

    //프로필 조회
    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserProfileResponse response = userProfileService.getProfile(userDetails.getUser());
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
    @PutMapping(value = "/all", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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