package com.back.matchduo.domain.usersearch.service;

import com.back.matchduo.domain.gameaccount.entity.GameAccount;
import com.back.matchduo.domain.post.service.PostGameProfileIconUrlBuilder;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.usersearch.dto.response.UserSearchListResponse;
import com.back.matchduo.domain.usersearch.repository.UserSearchGameAccountQueryRepository;
import com.back.matchduo.domain.usersearch.repository.UserSearchQueryRepository;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSearchService {

    private final UserSearchQueryRepository userSearchQueryRepository;
    private final UserSearchGameAccountQueryRepository gameAccountQueryRepository;
    private final PostGameProfileIconUrlBuilder iconUrlBuilder;

    public UserSearchListResponse search(String nickname, Long cursor, Integer size) {
        if (nickname == null || nickname.isBlank()) {
            throw new CustomException(CustomErrorCode.INVALID_SEARCH_KEYWORD);
        }

        int pageSize = (size == null || size <= 0) ? 10 : Math.min(size, 50);

        long totalCount = userSearchQueryRepository.countUsers(nickname);

        List<User> users = userSearchQueryRepository.findUsers(
                nickname, cursor, pageSize + 1
        );

        boolean hasNext = users.size() > pageSize;
        if (hasNext) {
            users.remove(users.size() - 1);
        }

        Long nextCursor = hasNext && !users.isEmpty()
                ? users.get(users.size() - 1).getId()
                : null;

        if (users.isEmpty()) {
            return new UserSearchListResponse(totalCount, List.of(), null, false);
        }

        List<Long> userIds = users.stream().map(User::getId).toList();

        List<GameAccount> accounts = gameAccountQueryRepository.findLolAccountsByUserIds(userIds);

        Map<Long, GameAccount> accountByUserId = accounts.stream()
                .collect(Collectors.toMap(
                        ga -> ga.getUser().getId(),
                        Function.identity(),
                        (a, b) -> a
                ));

        List<UserSearchListResponse.UserDto> result = new ArrayList<>();

        for (User u : users) {
            GameAccount ga = accountByUserId.get(u.getId());

            UserSearchListResponse.GameAccountDto gameAccountDto;

            if (ga == null) {
                gameAccountDto = UserSearchListResponse.GameAccountDto.notLinked();
            } else {
                String profileIconUrl = iconUrlBuilder.buildProfileIconUrl(ga.getProfileIconId());
                gameAccountDto = UserSearchListResponse.GameAccountDto.linked(
                        ga.getGameNickname(),
                        ga.getGameTag(),
                        profileIconUrl
                );
            }

            result.add(new UserSearchListResponse.UserDto(
                    u.getId(),
                    u.getNickname(),
                    u.getProfileImage(),
                    u.getComment(),
                    gameAccountDto
            ));
        }

        return new UserSearchListResponse(totalCount, result, nextCursor, hasNext);
    }
}