//package com.back.matchduo.global.config;
//
//import jakarta.annotation.PostConstruct;
//import jakarta.persistence.EntityManager;
//import lombok.RequiredArgsConstructor;
//import org.hibernate.Session;
//import org.springframework.stereotype.Component;
//
///**
// * Soft Delete된 데이터(is_active = false)가
// * 기본 조회(JPQL, Repository)에서 자동으로 제외되도록 Hibernate Filter를 활성화하는 설정
// */
//@Component
//@RequiredArgsConstructor
//public class SoftDeleteFilterConfig {
//
//    private final EntityManager entityManager;
//
//    @PostConstruct
//    public void enableFilter() {
//        // Hibernate Session 얻기
//        Session session = entityManager.unwrap(Session.class);
//
//        // softDeleteFilter 활성화 (is_active = true 조건을 전역적으로 적용)
//        session.enableFilter("softDeleteFilter");
//    }
//}