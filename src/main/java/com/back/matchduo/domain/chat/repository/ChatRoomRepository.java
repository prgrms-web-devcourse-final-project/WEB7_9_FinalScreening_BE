package com.back.matchduo.domain.chat.repository;

import com.back.matchduo.domain.chat.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 모집글 + 신청자 조합으로 채팅방 조회 (멱등 처리용)
    Optional<ChatRoom> findByPostIdAndSenderId(Long postId, Long senderId);

    // 내 채팅방 목록 (receiver 또는 sender로 참여)
    List<ChatRoom> findByReceiverIdOrSenderId(Long receiverId, Long senderId);

    // 커서 페이징용 (최신순)
    Page<ChatRoom> findByReceiverIdOrSenderIdOrderByUpdatedAtDesc(Long receiverId, Long senderId, Pageable pageable);
}