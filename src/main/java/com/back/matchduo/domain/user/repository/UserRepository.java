package com.back.matchduo.domain.user.repository;

import com.back.matchduo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Integer> {
}
