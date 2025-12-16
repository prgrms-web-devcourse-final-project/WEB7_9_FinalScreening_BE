package com.back.matchduo.domain.user.service;

import com.back.matchduo.domain.user.dto.request.UserSignUpRequest;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.repository.UserRepository;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserSignUpService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public void signUp(UserSignUpRequest request) {

        //이메일 중복 검사
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(CustomErrorCode.DUPLICATE_EMAIL);
        }

        //이메일 인증 여부 확인
        if (!emailService.isVerified(request.email())) {
            throw new CustomException(CustomErrorCode.EMAIL_NOT_VERIFIED);
        }

        //비밀번호 확인
        if (!request.password().equals(request.passwordConfirm())) {
            throw new CustomException(CustomErrorCode.WRONG_PASSWORD);
        }

        //닉네임 자동 생성
        String nickname = generateNickname(request.email());

        //User 생성
        User user = User.createUser(
                request.email(),
                request.password(),
                nickname
        );

        //저장
        userRepository.save(user);
    }
    //@앞 부분을 닉네임으로 저장
    private String generateNickname(String email) {
        String base = email.split("@")[0];
        String nickname = base;
        int idx = 1;//중복시 붙일 숫자

        while (userRepository.existsByNickname(nickname)) {
            nickname = base + "_" + idx++;
        }

        return nickname;
    }
}