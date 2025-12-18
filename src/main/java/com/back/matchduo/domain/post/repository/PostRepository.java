package com.back.matchduo.domain.post.repository;

import com.back.matchduo.domain.post.entity.Post;
import com.back.matchduo.domain.post.entity.PostStatus;
import com.back.matchduo.domain.post.entity.QueueType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 모집글 목록 조회 (무한 스크롤 - Cursor 기반)
    @Query("SELECT p FROM Post p WHERE " +
            "p.isActive = true AND " +
            "(:cursor IS NULL OR p.id < :cursor) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:queueType IS NULL OR p.queueType = :queueType) AND " +
            "(:gameModeId IS NULL OR p.gameMode.id = :gameModeId) AND " +
            "p.status <> com.back.matchduo.domain.post.entity.PostStatus.FINISHED " +
            "ORDER BY p.id DESC")
    List<Post> findPostsByFilters(
            @Param("cursor") Long cursor,
            @Param("status") PostStatus status,
            @Param("queueType") QueueType queueType,
            @Param("gameModeId") Long gameModeId,
            Pageable pageable
    );

    // ★ [여기 추가] 게시글 ID 목록으로 조회 + GameMode 함께 조회 (N+1 방지)
    // 내가 참여한 파티 리스트를 보여줄 때 사용합니다.
    @Query("SELECT p FROM Post p JOIN FETCH p.gameMode WHERE p.id IN :ids")
    List<Post> findAllByIdInWithGameMode(@Param("ids") List<Long> ids);
}
