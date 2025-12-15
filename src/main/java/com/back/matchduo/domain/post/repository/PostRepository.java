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
}