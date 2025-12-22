package com.back.matchduo.domain.gameaccount.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Data Dragon API 버전 관리 및 룬 정보 서비스
// 인메모리 캐싱을 사용하여 최신 버전과 룬 정보를 자동으로 갱신합니다.
@Slf4j
@Service
@RequiredArgsConstructor
public class DataDragonService {
    
    private static final String VERSIONS_API_URL = "https://ddragon.leagueoflegends.com/api/versions.json";
    private static final String RUNES_API_URL_TEMPLATE = "https://ddragon.leagueoflegends.com/cdn/%s/data/ko_KR/runesReforged.json";
    private static final Duration CACHE_DURATION = Duration.ofHours(24);
    private static final String DEFAULT_VERSION = "15.24.1"; // 기본값 (최신 버전으로 수동 업데이트 필요)
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private String cachedVersion;
    private LocalDateTime lastUpdated;
    
    // 룬 매핑 캐시: Map<StyleId, Map<PerkId, PerkKey>>
    private Map<Integer, Map<Integer, String>> runeMappings = new HashMap<>();
    // 스타일 매핑 캐시: Map<StyleId, StyleKey>
    private Map<Integer, String> styleMappings = new HashMap<>();
    
    // 애플리케이션 시작 시 즉시 최신 버전 및 룬 정보 가져오기
    @PostConstruct
    public void init() {
        refreshVersion();
        refreshRuneMappings();
    }
    
    // 주기적으로 최신 버전 및 룬 정보 갱신 (매일 자정)
    @Scheduled(cron = "0 0 0 * * *")
    public void scheduledUpdate() {
        refreshVersion();
        refreshRuneMappings();
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
    
    /**
     * 룬 정보 갱신 (Data Dragon API에서 최신 룬 정보 가져오기)
     */
    private void refreshRuneMappings() {
        try {
            String version = getLatestVersion();
            String runesUrl = String.format(RUNES_API_URL_TEMPLATE, version);
            
            // JSON 문자열로 받아서 ObjectMapper로 파싱
            String jsonResponse = restTemplate.getForObject(runesUrl, String.class);
            if (jsonResponse == null || jsonResponse.isEmpty()) {
                log.warn("룬 정보 조회 결과가 비어있습니다.");
                return;
            }
            
            List<RuneStyle> runeStyles = objectMapper.readValue(jsonResponse, 
                    new TypeReference<List<RuneStyle>>() {});
            
            if (runeStyles == null || runeStyles.isEmpty()) {
                log.warn("룬 정보 파싱 결과가 비어있습니다.");
                return;
            }
            
            // 매핑 초기화
            Map<Integer, Map<Integer, String>> newRuneMappings = new HashMap<>();
            Map<Integer, String> newStyleMappings = new HashMap<>();
            
            // 각 스타일별로 룬 정보 파싱
            for (RuneStyle style : runeStyles) {
                if (style.getId() == null || style.getKey() == null) {
                    continue;
                }
                
                // 스타일 매핑 저장
                newStyleMappings.put(style.getId(), style.getKey());
                
                // 각 스타일의 슬롯에서 룬 정보 추출
                Map<Integer, String> perkMap = new HashMap<>();
                if (style.getSlots() != null) {
                    for (RuneSlot slot : style.getSlots()) {
                        if (slot.getRunes() != null) {
                            for (Rune rune : slot.getRunes()) {
                                if (rune.getId() != null && rune.getKey() != null) {
                                    perkMap.put(rune.getId(), rune.getKey());
                                }
                            }
                        }
                    }
                }
                
                if (!perkMap.isEmpty()) {
                    newRuneMappings.put(style.getId(), perkMap);
                }
            }
            
            // 캐시 업데이트
            this.runeMappings = newRuneMappings;
            this.styleMappings = newStyleMappings;
            
            log.info("✅ 룬 정보 갱신 완료: 스타일 {}개, 총 룬 {}개", 
                    newStyleMappings.size(), 
                    newRuneMappings.values().stream().mapToInt(Map::size).sum());
        } catch (Exception e) {
            log.warn("룬 정보 갱신 실패: {}. 기존 매핑 유지", e.getMessage());
        }
    }
    
    /**
     * 스타일 ID로 스타일 이름(Key) 조회
     */
    public String getStyleName(Integer styleId) {
        if (styleId == null) {
            return null;
        }
        
        // 캐시에서 조회
        String styleName = styleMappings.get(styleId);
        if (styleName != null) {
            return styleName;
        }
        
        // 캐시에 없으면 갱신 시도
        if (runeMappings.isEmpty()) {
            refreshRuneMappings();
            styleName = styleMappings.get(styleId);
        }
        
        return styleName;
    }
    
    /**
     * 룬 ID로 룬 이름(Key) 조회
     */
    public String getPerkName(Integer styleId, Integer perkId) {
        if (styleId == null || perkId == null) {
            return null;
        }
        
        // 캐시에서 조회
        Map<Integer, String> perkMap = runeMappings.get(styleId);
        if (perkMap != null) {
            String perkName = perkMap.get(perkId);
            if (perkName != null) {
                return perkName;
            }
        }
        
        // 캐시에 없으면 갱신 시도
        if (runeMappings.isEmpty()) {
            refreshRuneMappings();
            perkMap = runeMappings.get(styleId);
            if (perkMap != null) {
                return perkMap.get(perkId);
            }
        }
        
        return null;
    }
    
    /**
     * Data Dragon API 룬 스타일 구조
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class RuneStyle {
        @JsonProperty("id")
        private Integer id;
        
        @JsonProperty("key")
        private String key;
        
        @JsonProperty("slots")
        private List<RuneSlot> slots;
    }
    
    /**
     * 룬 슬롯 구조
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class RuneSlot {
        @JsonProperty("runes")
        private List<Rune> runes;
    }
    
    /**
     * 룬 구조
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Rune {
        @JsonProperty("id")
        private Integer id;
        
        @JsonProperty("key")
        private String key;
    }
}

