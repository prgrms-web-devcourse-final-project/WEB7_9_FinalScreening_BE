package com.back.matchduo.domain.post.service;

import com.back.matchduo.domain.post.dto.request.PostCreateRequest;
import com.back.matchduo.domain.post.dto.request.PostStatusUpdateRequest;
import com.back.matchduo.domain.post.dto.request.PostUpdateRequest;
import com.back.matchduo.domain.post.dto.response.PostCreateResponse;
import com.back.matchduo.domain.post.dto.response.PostDeleteResponse;
import com.back.matchduo.domain.post.dto.response.PostListResponse;
import com.back.matchduo.domain.post.dto.response.PostStatusUpdateResponse;
import com.back.matchduo.domain.post.dto.response.PostUpdateResponse;
import com.back.matchduo.domain.post.entity.Post;
import com.back.matchduo.domain.post.entity.PostStatus;
import com.back.matchduo.domain.post.entity.QueueType;
import com.back.matchduo.domain.post.repository.PostRepository;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostValidator postValidator;
    private final PostListFacade postListFacade;

    // 모집글 생성
    @Transactional
    public PostCreateResponse createPost(PostCreateRequest request, Long userId) {
        return postListFacade.createPostWithPartyView(request, userId);
    }

    // 모집글 목록 조회
    public PostListResponse getPostList(
            Long cursor,
            Integer size,
            PostStatus status,
            QueueType queueType,
            Long gameModeId,
            String myPositions,
            String tier,
            Long currentUserId
    ) {
        return postListFacade.getPostList(cursor, size, status, queueType, gameModeId, myPositions, tier, currentUserId);
    }

    // 모집글 수정
    @Transactional
    public PostUpdateResponse updatePost(Long postId, PostUpdateRequest request, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.POST_NOT_FOUND));

        postValidator.validatePostOwner(post, userId);
        return postListFacade.updatePostWithPartyView(post, request);
    }

    // 상태 변경
    @Transactional
    public PostStatusUpdateResponse updatePostStatus(Long postId, PostStatusUpdateRequest request, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.POST_NOT_FOUND));

        postValidator.validatePostOwner(post, userId);
        postValidator.validateStatusUpdateAllowed(request.status());

        post.updateStatus(request.status());
        return PostStatusUpdateResponse.of(post);
    }

    // 모집글 단건 조회 (작성자 검증)
    public PostUpdateResponse getPostDetail(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.POST_NOT_FOUND));

        postValidator.validatePostOwner(post, userId);

        return postListFacade.buildPostDetailForEdit(post);
    }

    // 삭제
    @Transactional
    public PostDeleteResponse deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.POST_NOT_FOUND));

        postValidator.validatePostOwner(post, userId);
        post.deactivate();

        return PostDeleteResponse.of(postId);
    }
}
