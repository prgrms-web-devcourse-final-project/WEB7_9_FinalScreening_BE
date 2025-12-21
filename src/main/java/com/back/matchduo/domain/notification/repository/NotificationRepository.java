package com.back.matchduo.domain.notification.repository;

import com.back.matchduo.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification,Long> {

    Optional<Notification> findByIdAndReceiverId(Long id, Long userId);

    Page<Notification> findAllByReceiverId(Long receiverId, Pageable pageable);
}
