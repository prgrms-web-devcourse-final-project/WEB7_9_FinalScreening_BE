package com.back.matchduo.domain.user.service;

import com.back.matchduo.domain.user.dto.request.UserUpdateRequest;
import com.back.matchduo.domain.user.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

public class UserProfileService {
    private final PasswordEncoder passwordEncoder;
    private final ImageUploader imageUploader;

    public void updateUser(User user, UserUpdateRequest req) {

        if (req.email() != null) {
            user.changeEmail(req.email());
        }

        if (req.nickname() != null) {
            user.changeNickname(req.nickname());
        }

        if (req.comment() != null) {
            user.changeComment(req.comment());
        }

        //비밀번호 변경 요청이 있는 경우만
        if (isPasswordChangeRequest(req)) {
            changePassword(
                    user,
                    req.password(),
                    req.newPassword(),
                    req.newPasswordConfirm()
            );
        }
    }

    //프로필 이미지 업로드
    public String updateProfileImage(User user, MultipartFile file) {

        if (file == null || file.isEmpty()) {
            user.changeProfileImage(null);
            return null;
        }

        validateImage(file);

        String imageUrl = imageUploader.upload(file, "profile");
        user.changeProfileImage(imageUrl);

        return imageUrl;
    }

    private void changePassword(
            User user,
            String currentPassword,
            String newPassword,
            String newPasswordConfirm
    ) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        user.changePassword(passwordEncoder.encode(newPassword));
    }

    private boolean isPasswordChangeRequest(UserUpdateRequest req) {
        return req.password() != null
                || req.newPassword() != null
                || req.newPasswordConfirm() != null;
    }

    private void validateImage(MultipartFile file) {
        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("이미지 용량은 최대 10MB입니다.");
        }
    }
}
