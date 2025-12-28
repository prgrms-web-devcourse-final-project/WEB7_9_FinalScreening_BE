package com.back.matchduo.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "eventTaskExecutor")
    public Executor eventTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);       // 기본적으로 유지할 스레드 수
        executor.setMaxPoolSize(10);      // 최대 생성 가능한 스레드 수
        executor.setQueueCapacity(100);   // 대기 큐 크기
        executor.setThreadNamePrefix("Async-"); // 로그에서 확인할 스레드 이름 접두사
        executor.initialize();
        return executor;
    }
}