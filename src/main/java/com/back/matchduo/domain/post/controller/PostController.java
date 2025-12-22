package com.back.matchduo.domain.post.controller;

import com.back.matchduo.domain.post.dto.request.PostCreateRequest;
import com.back.matchduo.domain.post.dto.request.PostStatusUpdateRequest;
import com.back.matchduo.domain.post.dto.request.PostUpdateRequest;
import com.back.matchduo.domain.post.dto.response.PostCreateResponse;
import com.back.matchduo.domain.post.dto.response.PostDeleteResponse;
import com.back.matchduo.domain.post.dto.response.PostDetailResponse;
import com.back.matchduo.domain.post.dto.response.PostListResponse;
import com.back.matchduo.domain.post.dto.response.PostStatusUpdateResponse;
import com.back.matchduo.domain.post.dto.response.PostUpdateResponse;
import com.back.matchduo.domain.post.entity.PostStatus;
import com.back.matchduo.domain.post.entity.QueueType;
import com.back.matchduo.domain.post.service.PostService;
import com.back.matchduo.global.security.AuthPrincipal;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "모집글 생성", description = "새로운 듀오/파티 모집글을 생성합니다. (작성 시 파티 및 파티장이 자동으로 생성됩니다.)")
    public PostCreateResponse createPost(
            @Valid @RequestBody PostCreateRequest request
    ) {
        Long userId = AuthPrincipal.getUserId();
        return postService.createPost(request, userId);
    }

    // 모집글 목록 조회 (Cursor 기반 무한 스크롤)
    // 추가 필터: myPositions=TOP,JUNGLE (CSV), tier=DIAMOND (단일)
    @GetMapping
    @Operation(summary = "모집글 목록 조회", description = "필터링 조건에 따라 모집글 목록을 커서 기반 페이징으로 조회합니다.")
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
    @Operation(summary = "모집글 수정", description = "작성자가 자신의 모집글 내용을 수정합니다.")
    public PostUpdateResponse updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        Long userId = AuthPrincipal.getUserId();
        return postService.updatePost(postId, request, userId);
    }

    // 모집 상태 변경, FINISHED만 요청 가능
    @PatchMapping("/{postId}/status")
    @Operation(summary = "모집글 상태 변경", description = "모집글의 상태를 변경합니다. (예: 모집 완료 처리)")
    public PostStatusUpdateResponse updatePostStatus(
            @PathVariable Long postId,
            @Valid @RequestBody PostStatusUpdateRequest request
    ) {
        Long userId = AuthPrincipal.getUserId();
        return postService.updatePostStatus(postId, request, userId);
    }

    // 모집글 단건 조회 (작성자 검증)
    @GetMapping("/{postId}")
    @Operation(summary = "모집글 단건 조회", description = "특정 모집글의 상세 정보를 조회합니다.")
    public PostDetailResponse getPostDetail(
            @PathVariable Long postId
    ) {
        Long userId = AuthPrincipal.getUserId();
        return postService.getPostDetail(postId, userId);
    }

    // 모집글 삭제 (작성자만)
    @DeleteMapping("/{postId}")
    @Operation(summary = "모집글 삭제", description = "작성자가 자신의 모집글을 삭제합니다.")
    public PostDeleteResponse deletePost(
            @PathVariable Long postId
    ) {
        Long userId = AuthPrincipal.getUserId();
        return postService.deletePost(postId, userId);
    }
}
