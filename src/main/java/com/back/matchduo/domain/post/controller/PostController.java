package com.back.matchduo.domain.post.controller;

import com.back.matchduo.domain.post.dto.request.PostCreateRequest;
import com.back.matchduo.domain.post.dto.request.PostStatusUpdateRequest;
import com.back.matchduo.domain.post.dto.request.PostUpdateRequest;
import com.back.matchduo.domain.post.dto.response.PostCreateResponse;
import com.back.matchduo.domain.post.dto.response.PostDeleteResponse;
import com.back.matchduo.domain.post.dto.response.PostListResponse;
import com.back.matchduo.domain.post.dto.response.PostStatusUpdateResponse;
import com.back.matchduo.domain.post.dto.response.PostUpdateResponse;
import com.back.matchduo.domain.post.entity.PostStatus;
import com.back.matchduo.domain.post.entity.QueueType;
import com.back.matchduo.domain.post.service.PostService;
import com.back.matchduo.global.security.AuthPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    // 모집글 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostCreateResponse createPost(
            @Valid @RequestBody PostCreateRequest request
    ) {
        Long userId = AuthPrincipal.getUserId();
        return postService.createPost(request, userId);
    }

    // 모집글 목록 조회 (Cursor 기반 무한 스크롤)
    // 추가 필터: myPositions=TOP,JUNGLE (CSV), tier=DIAMOND (단일)
    @GetMapping
    public PostListResponse getPostList(
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) PostStatus status,
            @RequestParam(required = false) QueueType queueType,
            @RequestParam(required = false) Long gameModeId,
            @RequestParam(required = false) String myPositions,
            @RequestParam(required = false) String tier
    ) {
        Long currentUserId = null;
        try {
            currentUserId = AuthPrincipal.getUserId();
        } catch (Exception ignored) {
            // 비로그인 허용
        }

        return postService.getPostList(cursor, size, status, queueType, gameModeId, myPositions, tier, currentUserId);
    }

    // 모집글 수정 (작성자만)
    @PatchMapping("/{postId}")
    public PostUpdateResponse updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        Long userId = AuthPrincipal.getUserId();
        return postService.updatePost(postId, request, userId);
    }

    // 모집 상태 변경, FINISHED만 요청 가능
    @PatchMapping("/{postId}/status")
    public PostStatusUpdateResponse updatePostStatus(
            @PathVariable Long postId,
            @Valid @RequestBody PostStatusUpdateRequest request
    ) {
        Long userId = AuthPrincipal.getUserId();
        return postService.updatePostStatus(postId, request, userId);
    }

    // 모집글 삭제 (작성자만)
    @DeleteMapping("/{postId}")
    public PostDeleteResponse deletePost(
            @PathVariable Long postId
    ) {
        Long userId = AuthPrincipal.getUserId();
        return postService.deletePost(postId, userId);
    }
}
