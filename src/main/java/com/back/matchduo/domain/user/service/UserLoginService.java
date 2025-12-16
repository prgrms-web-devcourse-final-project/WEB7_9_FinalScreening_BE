package com.back.matchduo.domain.user.service;

import com.back.matchduo.domain.user.dto.request.UserLoginRequest;
import com.back.matchduo.domain.user.dto.response.UserLoginResponse;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.repository.UserRepository;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserLoginService {
    private final UserRepository userRepository;

    public UserLoginResponse login(UserLoginRequest request) {
        //이메일로 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new CustomException(CustomErrorCode.NOT_FOUND_USER)
                );

        //비밀번호
        if (!user.getPassword().equals(request.password())) {
            throw new CustomException(CustomErrorCode.WRONG_PASSWORD);
        }

        //로그인 성공
        return new UserLoginResponse(
                user.getId(),
                user.getEmail()
        );
    }
}