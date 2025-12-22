package com.back.matchduo.domain.user.service;

import com.back.matchduo.domain.user.dto.request.UserProfileRequest;
import com.back.matchduo.domain.user.dto.request.UserUpdateRequest;
import com.back.matchduo.domain.user.dto.response.UserProfileResponse;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.repository.UserRepository;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {
    private final UserRepository userRepository;
    private final FileService fileService;

    //프로필 조회
    public UserProfileResponse getProfile(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getProfileImage(),
                user.getNickname(),
                user.getComment()
        );
    }

    //프로필 수정
    public void updateProfile(User user, UserUpdateRequest request) {

        //기본 정보 수정
        if (request.email() != null) {
            user.setEmail(request.email());
        }

        if (request.nickname() != null) {
            user.setNickname(request.nickname());
        }

        if (request.comment() != null) {
            user.setComment(request.comment());
        }

        if (request.profile_image() != null) {
            user.setProfile_image(request.profile_image());
        }

        //비밀번호 처리
        handlePassword(user, request);
    }

    //비밀번호 변경 처리
    private void handlePassword(User user, UserUpdateRequest request) {

        boolean allEmpty =
                isBlank(request.password())
                        && isBlank(request.newPassword())
                        && isBlank(request.newPasswordConfirm());

        //전부 비어 있으면 → 그대로 유지
        if (allEmpty) {
            return;
        }

        //일부만 입력된 경우
        if (isBlank(request.password())
                || isBlank(request.newPassword())
                || isBlank(request.newPasswordConfirm())) {
            throw new CustomException(CustomErrorCode.PASSWORD_SHORTAGE);
        }

        //새 비밀번호 불일치
        if (!request.newPassword().equals(request.newPasswordConfirm())) {
            throw new CustomException(CustomErrorCode.PASSWORD_INCONSISTENCY);
        }

        //현재 비밀번호 검증 (평문 비교)
        if (!request.password().equals(user.getPassword())) {
            throw new CustomException(CustomErrorCode.WRONG_CURRENT_PASSWORD);
        }

        //비밀번호 변경
        user.setPassword(request.newPassword());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // 프로필 이미지 업데이트
    public void updateProfileWithFile(User user, UserUpdateRequest request, MultipartFile file) {
        updateProfile(user, request);

        if (file != null && !file.isEmpty()) {
            updateProfileImage(user, file);
        }
    }

    // 이미지 업로드
    @Transactional
    public void updateProfileImage(User user, MultipartFile file) {
        // 1. 파일 저장 및 웹 URL 경로 생성 (/images/uuid_파일명.png)
        String savedPath = fileService.upload(file);

        // 2. DB 업데이트 (영속성 컨텍스트 활용)
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));

        currentUser.updateProfileImage(savedPath);
    }
}