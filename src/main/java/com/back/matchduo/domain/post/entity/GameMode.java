package com.back.matchduo.domain.post.entity;


import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum GameMode {
    SUMMONERS_RIFT("소환사의 협곡"),
    HOWLING_ABYSS("칼바람 나락"),
    ARENA("아레나"); // 추후 추가 가능

    private final String description;
}