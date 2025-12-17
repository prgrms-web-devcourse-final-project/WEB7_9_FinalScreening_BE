package com.back.matchduo.domain.gameaccount.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;

// Data Dragon API 버전 관리 서비스
// 인메모리 캐싱을 사용하여 최신 버전을 자동으로 갱신합니다.
@Slf4j
@Service
@RequiredArgsConstructor
public class DataDragonService {
    
    private static final String VERSIONS_API_URL = "https://ddragon.leagueoflegends.com/api/versions.json";
    private static final Duration CACHE_DURATION = Duration.ofHours(24);
    private static final String DEFAULT_VERSION = "15.24.1"; // 기본값 (최신 버전으로 수동 업데이트 필요)
    
    private final RestTemplate restTemplate;
    
    private String cachedVersion;
    private LocalDateTime lastUpdated;
    
    // 애플리케이션 시작 시 즉시 최신 버전 가져오기
    @PostConstruct
    public void init() {
        refreshVersion();
    }
    
    // 주기적으로 최신 버전 갱신 (매일 자정)
    @Scheduled(cron = "0 0 0 * * *")
    public void scheduledUpdate() {
        refreshVersion();
    }
    
    // 최신 버전 조회
    // 캐시가 만료되었으면 자동으로 갱신
    public String getLatestVersion() {
        if (cachedVersion == null || isCacheExpired()) {
            refreshVersion();
        }
        return cachedVersion;
    }
    
    // 버전 갱신
    // Data Dragon API를 호출하여 최신 버전을 가져옵니다.
    private void refreshVersion() {
        try {
            String[] versions = restTemplate.getForObject(VERSIONS_API_URL, String[].class);
            if (versions != null && versions.length > 0) {
                cachedVersion = versions[0];
                lastUpdated = LocalDateTime.now();
                log.info("✅ Data Dragon 버전 갱신 완료: {}", cachedVersion);
            } else {
                log.warn("버전 조회 결과가 비어있습니다. 기본값 사용: {}", DEFAULT_VERSION);
                if (cachedVersion == null) {
                    cachedVersion = DEFAULT_VERSION;
                }
            }
        } catch (Exception e) {
            log.warn("Data Dragon 버전 갱신 실패: {}. 기존 버전 유지 또는 기본값 사용", e.getMessage());
            if (cachedVersion == null) {
                cachedVersion = DEFAULT_VERSION;
                log.info("기본 버전 사용: {}", DEFAULT_VERSION);
            }
        }
    }
    
    // 캐시 만료 여부 확인
    private boolean isCacheExpired() {
        return lastUpdated == null || 
               lastUpdated.isBefore(LocalDateTime.now().minus(CACHE_DURATION));
    }
}

