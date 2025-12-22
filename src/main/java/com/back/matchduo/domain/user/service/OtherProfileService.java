package com.back.matchduo.domain.user.service;

import com.back.matchduo.domain.gameaccount.entity.GameAccount;
import com.back.matchduo.domain.gameaccount.repository.GameAccountRepository;
import com.back.matchduo.domain.user.dto.response.OtherProfileResponse;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.repository.UserRepository;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OtherProfileService {

    private final UserRepository userRepository;
    private final GameAccountRepository gameAccountRepository;

    public OtherProfileResponse getOtherUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));

        GameAccount gameAccount = gameAccountRepository.findByUser_Id(userId)
                .stream()
                .findFirst()
                .orElse(null);

        return new OtherProfileResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImage(),
                user.getComment(),
                gameAccount != null ? gameAccount.getGameAccountId() : null
        );
    }
}