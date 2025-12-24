package com.back.matchduo.domain.user.service;

import com.back.matchduo.domain.user.dto.request.UserUpdatePasswordRequest;
import com.back.matchduo.domain.user.dto.response.UserProfileResponse;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.repository.UserRepository;
import com.back.matchduo.global.config.BaseUrlProperties;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {
    private final UserRepository userRepository;
    private final FileService fileService;
    private final BaseUrlProperties baseUrlProperties;

    public UserProfileResponse getProfile(User user) {
        User currentUser = findUser(user.getId());

        return UserProfileResponse.from(
                currentUser,
                baseUrlProperties.getBaseUrl() // 수정한 부분
        );
    }

    // 닉네임 수정
    private final List<String> bannedWords = List.of("씨발", "시발", "병신", "좆", "fuck", "ㅗ");

    public void updateNickname(User user, String nickname) {
        //공백 및 Null 체크
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new CustomException(CustomErrorCode.INVALID_NICKNAME_FORMAT);
        }

        //한글, 영어, 숫자 포함 2~8자 검사
        String nicknameRegex = "^[가-힣a-zA-Z0-9]{2,8}$";
        if (!nickname.matches(nicknameRegex)) {
            throw new CustomException(CustomErrorCode.INVALID_NICKNAME_FORMAT);
        }

        //비속어 체크
        String lowerNickname = nickname.toLowerCase();
        for (String banned : bannedWords) {
            // 비교 대상 비속어도 소문자로 변환하여 포함 여부 확인
            if (lowerNickname.contains(banned.toLowerCase())) {
                throw new CustomException(CustomErrorCode.BANNED_WORD_INCLUDED);
            }
        }

        User currentUser = findUser(user.getId());

        // 4. 중복 체크 (선택 사항: 이미 존재하는 닉네임인지 확인)
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(CustomErrorCode.DUPLICATE_NICKNAME);
        }

        currentUser.setNickname(nickname);
    }

    // 자기소개 수정
    public void updateComment(User user, String comment) {
        User currentUser = findUser(user.getId());
        currentUser.setComment(comment); // null일 경우에도 수정 가능하게 유지 (소개 삭제 기능)
    }

    //비밀번호 변경 처리
    public void updatePassword(User user, UserUpdatePasswordRequest request) {
        User currentUser = findUser(user.getId());
        // 1. 형식 체크
        if (isBlank(request.password()) || isBlank(request.newPassword()) ||
                isBlank(request.newPasswordConfirm()) || !isValidPassword(request.newPassword())) {
            throw new CustomException(CustomErrorCode.PASSWORD_SHORTAGE);
        }

        // 2. 새 비번 일치 체크
        if (!request.newPassword().equals(request.newPasswordConfirm())) {
            throw new CustomException(CustomErrorCode.PASSWORD_INCONSISTENCY);
        }

        // 3. 현재 비번 일치 체크
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

    //비밀번호 유효성 검사 로직
    private boolean isValidPassword(String password) {
        // 예: 8~20자, 영문, 숫자, 특수문자 포함 정규식
        String regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$";
        return password != null && password.matches(regex);
    }
}