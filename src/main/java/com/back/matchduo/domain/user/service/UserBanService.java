package com.back.matchduo.domain.user.service;

import com.back.matchduo.domain.user.dto.response.UserBlockListResponse;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.entity.UserBan;
import com.back.matchduo.domain.user.repository.UserBanRepository;
import com.back.matchduo.domain.user.repository.UserRepository;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBanService {

    private final UserBanRepository userBanRepository;
    private final UserRepository userRepository;

    // 차단
    @Transactional
    public void blockUser(Long fromUserId, Long toUserId) {
        if (fromUserId.equals(toUserId)) throw new CustomException(CustomErrorCode.BAN_MYSELF);

        User fromUser = userRepository.findById(fromUserId).orElseThrow();
        User toUser = userRepository.findById(toUserId).orElseThrow();

        if (userBanRepository.existsByFromUserAndToUser(fromUser, toUser)) return;

        userBanRepository.save(UserBan.createBan(fromUser, toUser));
    }

    // 차단 해제
    @Transactional
    public void unblockUser(Long fromUserId, Long toUserId) {
        User fromUser = userRepository.findById(fromUserId).orElseThrow();
        User toUser = userRepository.findById(toUserId).orElseThrow();

        UserBan ban = userBanRepository.findByFromUserAndToUser(fromUser, toUser)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_BANNED));

        userBanRepository.delete(ban);
    }

    // 차단 목록 조회
    public List<UserBlockListResponse> userBlockListResponses(Long userId) {
        User fromUser = userRepository.findById(userId).orElseThrow();

        return userBanRepository.findAllByFromUserOrderByCreatedAtDesc(fromUser).stream()
                .map(ban -> new UserBlockListResponse(
                        ban.getToUser().getId(),
                        ban.getToUser().getNickname(),
                        ban.getToUser().getProfileImage(),
                        ban.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}