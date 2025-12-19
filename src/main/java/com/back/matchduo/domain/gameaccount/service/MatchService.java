package com.back.matchduo.domain.gameaccount.service;

import com.back.matchduo.domain.gameaccount.client.RiotApiClient;
import com.back.matchduo.domain.gameaccount.dto.RiotApiDto;
import com.back.matchduo.domain.gameaccount.dto.response.MatchResponse;
import java.util.ArrayList;
import com.back.matchduo.domain.gameaccount.entity.GameAccount;
import com.back.matchduo.domain.gameaccount.entity.Match;
import com.back.matchduo.domain.gameaccount.entity.MatchParticipant;
import com.back.matchduo.domain.gameaccount.repository.GameAccountRepository;
import com.back.matchduo.domain.gameaccount.repository.MatchParticipantRepository;
import com.back.matchduo.domain.gameaccount.repository.MatchRepository;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchParticipantRepository matchParticipantRepository;
    private final GameAccountRepository gameAccountRepository;
    private final RiotApiClient riotApiClient;
    private final DataDragonService dataDragonService;
    private final ObjectMapper objectMapper;

    private static final int DEFAULT_MATCH_COUNT = 20;
    
    // 허용된 큐 ID 목록 (랭크 게임: 400=솔로랭크, 420=자유랭크, 430=일반, 440=자유랭크, 450=칼바람)
    private static final List<Integer> ALLOWED_QUEUE_IDS = List.of(400, 420, 430, 440, 450);

    /**
     * 전적 갱신 (Riot API 호출 → DB 저장)
     * @param gameAccountId 게임 계정 ID
     * @param userId 인증된 사용자 ID (로그용)
     * @param count 조회할 매치 개수
     * @return 저장된 매치 정보 목록
     */
    public List<MatchResponse> refreshMatchHistory(Long gameAccountId, Long userId, int count) {
        // 게임 계정 조회
        GameAccount gameAccount = gameAccountRepository.findById(gameAccountId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GAME_ACCOUNT_NOT_FOUND));

        // puuid가 없으면 매치 정보를 가져올 수 없음
        if (gameAccount.getPuuid() == null || gameAccount.getPuuid().isEmpty()) {
            throw new CustomException(CustomErrorCode.GAME_ACCOUNT_NO_PUUID);
        }

        // Riot API 호출하여 매치 ID 목록 조회
        List<String> matchIds;
        try {
            matchIds = riotApiClient.getMatchIdsByPuuid(gameAccount.getPuuid(), 0, count);
            log.info("Riot API 호출 성공: gameAccountId={}, 매치 ID 개수={}", 
                    gameAccountId, matchIds != null ? matchIds.size() : 0);
        } catch (Exception e) {
            log.error("Riot API 호출 실패: gameAccountId={}, error={}", gameAccountId, e.getMessage());
            throw new CustomException(CustomErrorCode.MATCH_FETCH_FAILED, e);
        }

        if (matchIds == null || matchIds.isEmpty()) {
            log.warn("매치 정보가 없습니다: gameAccountId={}", gameAccountId);
            return List.of();
        }

        // 각 매치 상세 정보 조회 및 저장
        int savedCount = 0;
        for (String matchId : matchIds) {
            try {
                // 매치 상세 정보 조회
                RiotApiDto.MatchResponse matchResponse = riotApiClient.getMatchByMatchId(matchId);
                
                // QueueId 필터링 (400, 420, 430, 440, 450만 허용)
                Integer queueId = matchResponse.getInfo().getQueueId();
                if (queueId == null || !ALLOWED_QUEUE_IDS.contains(queueId)) {
                    log.debug("허용되지 않은 큐 ID: matchId={}, queueId={}", matchId, queueId);
                    continue;  // 스킵
                }
                
                // 중복 체크
                if (matchRepository.existsByRiotMatchIdAndGameAccount_GameAccountId(matchId, gameAccountId)) {
                    log.debug("이미 저장된 매치: matchId={}, gameAccountId={}", matchId, gameAccountId);
                    continue;  // 스킵
                }

                // 해당 puuid의 participant 찾기
                RiotApiDto.MatchResponse.Participant participant = matchResponse.getInfo().getParticipants().stream()
                        .filter(p -> p.getPuuid().equals(gameAccount.getPuuid()))
                        .findFirst()
                        .orElse(null);

                if (participant == null) {
                    log.warn("매치에 해당 플레이어가 없습니다: matchId={}, puuid={}", matchId, gameAccount.getPuuid());
                    continue;
                }

                // KDA 계산
                double kda = calculateKda(participant.getKills(), participant.getDeaths(), participant.getAssists());

                // 룬 정보 JSON 변환
                String perksJson = convertPerksToJson(participant.getPerks());

                // Match 엔티티 생성 및 저장
                Match match = Match.builder()
                        .riotMatchId(matchId)
                        .gameAccount(gameAccount)
                        .queueId(matchResponse.getInfo().getQueueId())
                        .gameStartTimestamp(matchResponse.getInfo().getGameStartTimestamp())
                        .gameDuration(matchResponse.getInfo().getGameDuration())
                        .win(participant.getWin())
                        .build();
                Match savedMatch = matchRepository.save(match);

                // MatchParticipant 엔티티 생성 및 저장
                MatchParticipant matchParticipant = MatchParticipant.builder()
                        .match(savedMatch)
                        .gameAccount(gameAccount)
                        .championId(participant.getChampionId())
                        .championName(participant.getChampionName())
                        .spell1Id(participant.getSummoner1Id())
                        .spell2Id(participant.getSummoner2Id())
                        .kills(participant.getKills())
                        .deaths(participant.getDeaths())
                        .assists(participant.getAssists())
                        .kda(kda)
                        .cs(participant.getTotalMinionsKilled())
                        .level(participant.getChampLevel())
                        .item0(participant.getItem0())
                        .item1(participant.getItem1())
                        .item2(participant.getItem2())
                        .item3(participant.getItem3())
                        .item4(participant.getItem4())
                        .item5(participant.getItem5())
                        .item6(participant.getItem6())
                        .perks(perksJson)
                        .build();
                matchParticipantRepository.save(matchParticipant);

                savedCount++;
                log.debug("매치 저장 완료: matchId={}, gameAccountId={}", matchId, gameAccountId);
            } catch (Exception e) {
                log.error("매치 저장 실패: matchId={}, gameAccountId={}, error={}", 
                        matchId, gameAccountId, e.getMessage());
                // 실패한 매치는 스킵하고 계속 진행
            }
        }

        log.info("매치 정보 갱신 완료: gameAccountId={}, 요청자 userId={}, 저장된 매치 개수={}", 
                gameAccountId, userId, savedCount);

        // 저장된 매치 정보 조회하여 반환
        return getRecentMatches(gameAccountId, userId, count);
    }

    /**
     * 최근 매치 조회 (DB에서 조회)
     * @param gameAccountId 게임 계정 ID
     * @param userId 인증된 사용자 ID (로그용)
     * @param count 조회할 매치 개수
     * @return 매치 정보 목록
     */
    @Transactional(readOnly = true)
    public List<MatchResponse> getRecentMatches(Long gameAccountId, Long userId, int count) {
        // 게임 계정 조회
        GameAccount gameAccount = gameAccountRepository.findById(gameAccountId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GAME_ACCOUNT_NOT_FOUND));

        // DB에서 최근 매치 조회
        List<Match> matches = matchRepository.findByGameAccount_GameAccountIdOrderByGameStartTimestampDesc(gameAccountId);
        
        // 요청한 개수만큼만 반환
        List<Match> limitedMatches = matches.stream()
                .limit(count)
                .collect(Collectors.toList());

        // Data Dragon 버전 가져오기
        String version = dataDragonService.getLatestVersion();

        // MatchResponse로 변환
        return limitedMatches.stream()
                .map(match -> {
                    MatchParticipant participant = matchParticipantRepository.findByMatch_MatchId(match.getMatchId())
                            .orElse(null);

                    if (participant == null) {
                        log.warn("매치 참가자 정보가 없습니다: matchId={}", match.getMatchId());
                        return null;
                    }

                    return convertToMatchResponse(match, participant, version);
                })
                .filter(match -> match != null)
                .collect(Collectors.toList());
    }

    /**
     * Match와 MatchParticipant를 MatchResponse로 변환
     */
    private MatchResponse convertToMatchResponse(Match match, MatchParticipant participant, String version) {
        // 이미지 URL 생성
        String championImageUrl = getChampionImageUrl(participant.getChampionName(), version);
        String spell1ImageUrl = getSpellImageUrl(participant.getSpell1Id(), version);
        String spell2ImageUrl = getSpellImageUrl(participant.getSpell2Id(), version);
        List<String> itemImageUrls = getItemImageUrls(
                participant.getItem0(), participant.getItem1(), participant.getItem2(),
                participant.getItem3(), participant.getItem4(), participant.getItem5(),
                participant.getItem6(), version
        );
        List<String> perkImageUrls = getPerkImageUrls(participant.getPerks());

        // 시간 포맷팅
        String gameStartTimeFormatted = formatGameStartTime(match.getGameStartTimestamp());
        String gameDurationFormatted = formatGameDuration(match.getGameDuration());

        return MatchResponse.builder()
                .matchId(match.getRiotMatchId())
                .queueId(match.getQueueId())
                .gameStartTimestamp(match.getGameStartTimestamp())
                .gameStartTimeFormatted(gameStartTimeFormatted)
                .gameDuration(match.getGameDuration())
                .gameDurationFormatted(gameDurationFormatted)
                .win(match.getWin())
                .championId(participant.getChampionId())
                .championName(participant.getChampionName())
                .championImageUrl(championImageUrl)
                .spell1Id(participant.getSpell1Id())
                .spell1ImageUrl(spell1ImageUrl)
                .spell2Id(participant.getSpell2Id())
                .spell2ImageUrl(spell2ImageUrl)
                .perks(participant.getPerks())
                .perkImageUrls(perkImageUrls)
                .kills(participant.getKills())
                .deaths(participant.getDeaths())
                .assists(participant.getAssists())
                .kda(participant.getKda())
                .cs(participant.getCs())
                .level(participant.getLevel())
                .items(List.of(
                        participant.getItem0(), participant.getItem1(), participant.getItem2(),
                        participant.getItem3(), participant.getItem4(), participant.getItem5(),
                        participant.getItem6()
                ))
                .itemImageUrls(itemImageUrls)
                .build();
    }

    /**
     * KDA 계산
     */
    private double calculateKda(Integer kills, Integer deaths, Integer assists) {
        if (deaths == 0) {
            return kills + assists;
        }
        return (double)(kills + assists) / deaths;
    }

    /**
     * 룬 정보를 JSON 문자열로 변환
     */
    private String convertPerksToJson(RiotApiDto.MatchResponse.Participant.Perks perks) {
        if (perks == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(perks);
        } catch (JsonProcessingException e) {
            log.warn("룬 정보 JSON 변환 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 챔피언 이미지 URL 생성
     */
    private String getChampionImageUrl(String championName, String version) {
        if (championName == null || championName.isEmpty()) {
            return null;
        }
        return String.format("https://ddragon.leagueoflegends.com/cdn/%s/img/champion/%s.png", 
                version, championName);
    }

    /**
     * 스펠 이미지 URL 생성
     */
    private String getSpellImageUrl(Integer spellId, String version) {
        if (spellId == null) {
            return null;
        }
        String spellName = getSpellName(spellId);
        if (spellName == null) {
            return null;
        }
        return String.format("https://ddragon.leagueoflegends.com/cdn/%s/img/spell/Summoner%s.png", 
                version, spellName);
    }

    /**
     * 스펠 ID를 스펠 이름으로 변환
     */
    private String getSpellName(Integer spellId) {
        if (spellId == null) {
            return null;
        }
        return switch (spellId) {
            case 1 -> "Boost";
            case 3 -> "Exhaust";
            case 4 -> "Flash";
            case 6 -> "Haste";
            case 7 -> "Heal";
            case 11 -> "Smite";
            case 12 -> "Teleport";
            case 13 -> "Mana";
            case 14 -> "Ignite";
            case 21 -> "Barrier";
            default -> null;
        };
    }

    /**
     * 아이템 이미지 URL 배열 생성
     */
    private List<String> getItemImageUrls(Integer item0, Integer item1, Integer item2,
                                         Integer item3, Integer item4, Integer item5,
                                         Integer item6, String version) {
        List<String> itemUrls = new ArrayList<>();
        itemUrls.add(getItemImageUrl(item0, version));
        itemUrls.add(getItemImageUrl(item1, version));
        itemUrls.add(getItemImageUrl(item2, version));
        itemUrls.add(getItemImageUrl(item3, version));
        itemUrls.add(getItemImageUrl(item4, version));
        itemUrls.add(getItemImageUrl(item5, version));
        itemUrls.add(getItemImageUrl(item6, version));
        return itemUrls;
    }

    /**
     * 아이템 이미지 URL 생성 (itemId가 0이면 null)
     */
    private String getItemImageUrl(Integer itemId, String version) {
        if (itemId == null || itemId == 0) {
            return null;
        }
        return String.format("https://ddragon.leagueoflegends.com/cdn/%s/img/item/%d.png", 
                version, itemId);
    }

    /**
     * 룬 이미지 URL 배열 생성
     * 주 룬과 부 룬의 메인 룬만 반환 (op.gg 스타일)
     * 예: 정복자(주 룬) + 지배(부 룬) → 2개의 이미지만 반환
     */
    private List<String> getPerkImageUrls(String perksJson) {
        if (perksJson == null || perksJson.isEmpty()) {
            return List.of();
        }
        try {
            RiotApiDto.MatchResponse.Participant.Perks perks = objectMapper.readValue(
                    perksJson, RiotApiDto.MatchResponse.Participant.Perks.class);
            
            List<String> imageUrls = new ArrayList<>();
            
            // 주 룬 스타일의 메인 룬 이미지
            if (perks.getStyles() != null && !perks.getStyles().isEmpty()) {
                RiotApiDto.MatchResponse.Participant.Perks.PerkStyle primaryStyle = perks.getStyles().get(0);
                if (primaryStyle != null 
                        && primaryStyle.getStyle() != null 
                        && primaryStyle.getSelections() != null 
                        && !primaryStyle.getSelections().isEmpty()) {
                    Integer primaryPerk = primaryStyle.getSelections().get(0).getPerk();
                    if (primaryPerk != null) {
                        // 주 룬 메인 룬 이미지 URL 생성
                        String styleName = getStyleName(primaryStyle.getStyle());
                        String perkName = getPerkName(primaryStyle.getStyle(), primaryPerk);
                        if (styleName != null && perkName != null) {
                            // 특정 룬은 파일명에 "Temp" 접미사 필요
                            String fileName = needsTempSuffix(primaryStyle.getStyle(), primaryPerk) 
                                    ? perkName + "Temp" 
                                    : perkName;
                            imageUrls.add(String.format(
                                    "https://ddragon.leagueoflegends.com/cdn/img/perk-images/Styles/%s/%s/%s.png",
                                    styleName, perkName, fileName
                            ));
                        }
                    }
                }
            }
            
            // 부 룬 스타일 이미지
            if (perks.getStyles() != null && perks.getStyles().size() > 1) {
                RiotApiDto.MatchResponse.Participant.Perks.PerkStyle subStyle = perks.getStyles().get(1);
                if (subStyle != null && subStyle.getStyle() != null) {
                    // 부 룬 스타일 이미지 URL 생성
                    String subStyleName = getSubStyleName(subStyle.getStyle());
                    Integer subStyleImageId = getSubStyleImageId(subStyle.getStyle());
                    if (subStyleName != null && subStyleImageId != null) {
                        imageUrls.add(String.format(
                                "https://ddragon.leagueoflegends.com/cdn/img/perk-images/Styles/%d_%s.png",
                                subStyleImageId, subStyleName
                        ));
                    }
                }
            }
            
            // 최대 2개의 이미지만 반환
            return imageUrls.size() > 2 ? imageUrls.subList(0, 2) : imageUrls;
        } catch (JsonProcessingException e) {
            log.warn("룬 정보 JSON 파싱 실패: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 스타일 ID를 스타일 이름으로 변환
     * Data Dragon API에서 동적으로 가져온 매핑 사용
     */
    private String getStyleName(Integer styleId) {
        if (styleId == null) {
            return null;
        }
        
        return dataDragonService.getStyleName(styleId);
    }

    /**
     * 룬 ID를 룬 이름으로 변환
     * Data Dragon API에서 동적으로 가져온 매핑 사용
     */
    private String getPerkName(Integer styleId, Integer perkId) {
        if (styleId == null || perkId == null) {
            return null;
        }
        
        return dataDragonService.getPerkName(styleId, perkId);
    }

    /**
     * 부룬 스타일 이미지 ID 반환
     * JSON 데이터 기준:
     * - Precision (8000) → 7201
     * - Domination (8100) → 7200
     * - Sorcery (8200) → 7202
     * - Inspiration (8300) → 7203
     * - Resolve (8400) → 7204
     */
    private Integer getSubStyleImageId(Integer styleId) {
        if (styleId == null) {
            return null;
        }
        return switch (styleId) {
            case 8000 -> 7201;  // Precision
            case 8100 -> 7200;  // Domination
            case 8200 -> 7202;  // Sorcery
            case 8300 -> 7203;  // Inspiration
            case 8400 -> 7204;  // Resolve
            default -> null;
        };
    }

    /**
     * 부룬 스타일 파일명 반환
     * Inspiration의 경우 "Whimsy"를 사용 (7203_Whimsy.png)
     */
    private String getSubStyleName(Integer styleId) {
        if (styleId == null) {
            return null;
        }
        return switch (styleId) {
            case 8000 -> "Precision";
            case 8100 -> "Domination";
            case 8200 -> "Sorcery";
            case 8300 -> "Whimsy";  // Inspiration은 부룬에서 Whimsy 사용
            case 8400 -> "Resolve";
            default -> null;
        };
    }

    /**
     * 특정 룬이 파일명에 "Temp" 접미사가 필요한지 확인
     * 예: LethalTempo는 LethalTempoTemp.png 사용
     */
    private boolean needsTempSuffix(Integer styleId, Integer perkId) {
        if (styleId == null || perkId == null) {
            return false;
        }
        
        // Precision 스타일의 LethalTempo (8008)만 Temp 접미사 필요
        if (styleId == 8000 && perkId == 8008) {
            return true;
        }
        
        // 다른 룬들도 필요시 여기에 추가
        return false;
    }

    /**
     * 게임 시작 시간 포맷팅
     * @param timestamp 밀리초 타임스탬프
     * @return 포맷팅된 시간 문자열 (예: "30분 전", "2시간 전", "3일 전", "1개월 전", "2개월 전")
     */
    private String formatGameStartTime(Long timestamp) {
        if (timestamp == null) {
            return null;
        }

        Instant gameTime = Instant.ofEpochMilli(timestamp);
        Instant now = Instant.now();
        long minutesAgo = ChronoUnit.MINUTES.between(gameTime, now);

        // 1시간 미만: "30분 전"
        if (minutesAgo < 60) {
            return minutesAgo + "분 전";
        }
        
        // 24시간 미만: "2시간 전"
        long hoursAgo = minutesAgo / 60;
        if (hoursAgo < 24) {
            return hoursAgo + "시간 전";
        }
        
        // 일 단위 계산
        LocalDateTime gameDateTime = LocalDateTime.ofInstant(gameTime, ZoneId.of("Asia/Seoul"));
        LocalDateTime nowDateTime = LocalDateTime.ofInstant(now, ZoneId.of("Asia/Seoul"));
        long daysAgo = ChronoUnit.DAYS.between(gameDateTime, nowDateTime);
        
        // 한달 미만: "3일 전"
        if (daysAgo < 30) {
            return daysAgo + "일 전";
        }
        
        // 달 단위 계산
        long monthsAgo = ChronoUnit.MONTHS.between(gameDateTime, nowDateTime);
        
        // 한달 이상
        if (monthsAgo < 12) {
            return monthsAgo + "개월 전";
        } else {
            // 1년 이상이면 절대 시간 표시
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return gameDateTime.format(formatter);
        }
    }

    /**
     * 게임 진행 시간 포맷팅
     * @param durationSeconds 게임 진행 시간 (초)
     * @return 포맷팅된 시간 문자열 (예: "25분 30초" 또는 "45:30")
     */
    private String formatGameDuration(Integer durationSeconds) {
        if (durationSeconds == null) {
            return null;
        }

        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;

        if (minutes > 0) {
            return String.format("%d분 %d초", minutes, seconds);
        } else {
            return String.format("%d초", seconds);
        }
    }
}

