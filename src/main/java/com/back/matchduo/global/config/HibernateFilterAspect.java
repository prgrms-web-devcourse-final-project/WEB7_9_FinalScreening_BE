//package com.back.matchduo.global.config;
//
//import jakarta.persistence.EntityManager;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.hibernate.Session;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.springframework.stereotype.Component;
//
//@Aspect
//@Component
//public class HibernateFilterAspect {
//
//    private final EntityManager entityManager;
//
//    public HibernateFilterAspect(EntityManager entityManager) {
//        this.entityManager = entityManager;
//    }
//
//    // 서비스나 레포지토리 등 트랜잭션이 일어나는 곳을 감싸서 실행
//    // 보통 Service 계층의 메소드 실행 시점에 적용합니다.
//    @Around("execution(* com.back.matchduo..service..*(..))")
//    public Object enableFilter(ProceedingJoinPoint joinPoint) throws Throwable {
//        // 1. 현재 트랜잭션의 세션을 가져옴
//        Session session = entityManager.unwrap(Session.class);
//
//        // 2. 필터 활성화
//        session.enableFilter("softDeleteFilter");
//
//        // 3. 원래 메소드 실행
//        return joinPoint.proceed();
//    }
//}