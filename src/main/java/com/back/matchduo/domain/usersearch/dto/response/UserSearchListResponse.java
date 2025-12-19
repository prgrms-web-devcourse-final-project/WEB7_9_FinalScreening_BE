package com.back.matchduo.domain.usersearch.dto.response;

import java.util.List;

public record UserSearchListResponse(
        long totalCount,
        List<UserDto> users,
        Long nextCursor,
        Boolean hasNext
) {
    public record UserDto(
            Long userId,
            String nickname,
            String profileImageUrl,
            String comment,
            GameAccountDto gameAccount
    ) {}

    public record GameAccountDto(
            boolean linked,
            String gameName,
            String tagLine,
            String profileIconUrl
    ) {
        public static GameAccountDto notLinked() {
            return new GameAccountDto(false, null, null, null);
        }

        public static GameAccountDto linked(String gameName, String tagLine, String profileIconUrl) {
            return new GameAccountDto(true, gameName, tagLine, profileIconUrl);
        }
    }
}
