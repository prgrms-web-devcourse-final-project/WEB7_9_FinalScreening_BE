package com.back.matchduo.domain.user.service;

import com.back.matchduo.domain.user.dto.request.UserProfileRequest;
import com.back.matchduo.domain.user.dto.request.UserUpdateRequest;
import com.back.matchduo.domain.user.entity.User;
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
    //프로필 조회
    public UserProfileRequest getProfile(User user) {
        return new UserProfileRequest(
                user.getEmail(),
                user.getProfile_image(),
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

        // 🔹 비밀번호 처리
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
    private void updateProfileImage(User user, MultipartFile file) {
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB 제한
            throw new IllegalArgumentException("이미지 파일은 최대 10MB까지 업로드 가능합니다.");
        }
        Path uploadDir = Paths.get("upload/profile/"); // 업로드 폴더 경로 지정
        try {
            Files.createDirectories(uploadDir); // 폴더 생성

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File dest = uploadDir.resolve(fileName).toFile(); // Path → File 변환
            file.transferTo(dest); // transferTo(File)

            user.setProfile_image("/upload/profile/" + fileName);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}