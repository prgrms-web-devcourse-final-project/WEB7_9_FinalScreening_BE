package com.back.matchduo.domain.usersearch.controller;

import com.back.matchduo.domain.usersearch.service.UserSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.back.matchduo.domain.usersearch.dto.response.UserSearchListResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserSearchController {

    private final UserSearchService userSearchService;

    @GetMapping("/search")
    public UserSearchListResponse searchUsers(
            @RequestParam String nickname,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) Integer size
    ) {
        return userSearchService.search(nickname, cursor, size);
    }
}
