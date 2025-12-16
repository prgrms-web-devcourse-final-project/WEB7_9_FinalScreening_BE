package com.back.matchduo.domain.user.repository;

import com.back.matchduo.domain.user.entity.Verification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationRepository extends JpaRepository<Verification, Long> {
    Optional<Verification> findByEmail(String email);
}
