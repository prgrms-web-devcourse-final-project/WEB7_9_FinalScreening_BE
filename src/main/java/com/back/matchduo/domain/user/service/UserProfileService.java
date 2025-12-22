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

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {
    private final UserRepository userRepository;
    private final FileService fileService;

    //프로필 조회
    public UserProfileRequest getProfile(User user) {
        return new UserProfileRequest(
                user.getEmail(),
                user.getProfileImage(),
                user.getNickname(),
                user.getComment()
        );
    }

    // 닉네임 수정
    public void updateNickname(User user, String nickname) {
        if (nickname == null || nickname.isBlank()) throw new CustomException(CustomErrorCode.INVALID_REQUEST);
        User currentUser = findUser(user.getId());
        currentUser.setNickname(nickname);
    }

    // 자기소개 수정
    public void updateComment(User user, String comment) {
        User currentUser = findUser(user.getId());
        currentUser.setComment(comment); // null일 경우에도 수정 가능하게 유지 (소개 삭제 기능)
    }

    //비밀번호 변경 처리
    public void updatePassword(User user, UserUpdateRequest request) {
        User currentUser = findUser(user.getId());
        //비밀번호 조건 미완료
        if (isBlank(request.password()) || isBlank(request.newPassword()) || isBlank(request.newPasswordConfirm())) {
            throw new CustomException(CustomErrorCode.PASSWORD_SHORTAGE);
        }
        //비밀번호 불일치
        if (!request.newPassword().equals(request.newPasswordConfirm())) {
            throw new CustomException(CustomErrorCode.PASSWORD_INCONSISTENCY);
        }
        //현재 비밀번호 불일치
        if (!request.password().equals(currentUser.getPassword())) {
            throw new CustomException(CustomErrorCode.WRONG_CURRENT_PASSWORD);
        }
        //비밀번호 작성
        currentUser.setPassword(request.newPassword());
    }

    // 이미지 업로드
    @Transactional
    public void updateProfileImage(User user, MultipartFile file) {
        // 1. 파일 저장 및 웹 URL 경로 생성 (/images/uuid_파일명.png)
        String savedPath = fileService.upload(file);

        // 2. DB 업데이트 (영속성 컨텍스트 활용)
        User currentUser = findUser(user.getId());

        currentUser.updateProfileImage(savedPath);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));
    }
}