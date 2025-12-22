package com.back.matchduo.domain.post.service;

import com.back.matchduo.domain.party.entity.Party;
import com.back.matchduo.domain.party.entity.PartyMember;
import com.back.matchduo.domain.party.entity.PartyMemberRole;
import com.back.matchduo.domain.gameaccount.entity.FavoriteChampion;
import com.back.matchduo.domain.gameaccount.entity.GameAccount;
import com.back.matchduo.domain.gameaccount.entity.MatchParticipant;
import com.back.matchduo.domain.gameaccount.entity.Rank;
import com.back.matchduo.domain.gameaccount.service.DataDragonService;
import com.back.matchduo.domain.party.repository.PartyMemberRepository;
import com.back.matchduo.domain.party.repository.PartyRepository;
import com.back.matchduo.domain.post.dto.request.PostCreateRequest;
import com.back.matchduo.domain.post.dto.request.PostUpdateRequest;
import com.back.matchduo.domain.post.dto.response.*;
import com.back.matchduo.domain.post.entity.*;
import com.back.matchduo.domain.post.repository.GameModeRepository;
import com.back.matchduo.domain.post.repository.PostGameAccountQueryRepository;
import com.back.matchduo.domain.post.repository.PostListQueryRepository;
import com.back.matchduo.domain.post.repository.PostPartyQueryRepository;
import com.back.matchduo.domain.post.repository.PostRepository;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.repository.UserRepository;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostListFacade {

    private final PostRepository postRepository;
    private final GameModeRepository gameModeRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

    private final PartyRepository partyRepository;
    private final PartyMemberRepository partyMemberRepository;

    private final PostValidator postValidator;
    private final PostListQueryRepository postListQueryRepository;
    private final PostPartyQueryRepository postPartyQueryRepository;
    private final PostGameAccountQueryRepository postGameAccountQueryRepository;
    private final PostGameProfileIconUrlBuilder iconUrlBuilder;
    private final DataDragonService dataDragonService;

    // 모집글 생성 + 화면에 필요한 최소 파티 표시(작성자만)
    @Transactional
    public PostCreateResponse createPostWithPartyView(PostCreateRequest request, Long userId) {
        // 생성 규칙 검증
        postValidator.validateCreate(request);

        // 2) 게임모드
        GameMode gameMode = gameModeRepository.findById(request.gameModeId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.GAME_MODE_NOT_FOUND));

        // lookingPositions 직렬화
        String lookingPositionsJson = serializePositions(request.lookingPositions());

        // User 연결
        User writerRef = getUserReference(userId);

        // Post 저장
        Post post = Post.builder()
                .user(writerRef)
                .gameMode(gameMode)
                .queueType(request.queueType())
                .myPosition(request.myPosition())
                .lookingPositions(lookingPositionsJson)
                .mic(request.mic())
                .recruitCount(request.recruitCount())
                .memo(request.memo())
                .build();

        Post saved = postRepository.save(post);

        // 1. 파티 생성 및 저장
        Party party = new Party(saved.getId(), userId);
        partyRepository.save(party);

        PartyMember leader = PartyMember.builder()
                .party(party)
                .user(writerRef)
                .role(PartyMemberRole.LEADER)
                .build();
        partyMemberRepository.save(leader);

        // 생성 직후 participants는 최소 작성자 1명으로 표시
        List<Position> lookingPositions = request.lookingPositions();

        User writer = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.POST_FORBIDDEN));

        PostWriter writerDto = buildWriterDto(writer, null, null);

        // role은 응답 명세서 기준 "LEADER"/"MEMBER" 문자열로 내림
        List<PostParticipant> participants = List.of(
                new PostParticipant(
                        writer.getId(),
                        writer.getNickname(),
                        writer.getProfileImage(),
                        PartyMemberRole.LEADER.name()
                )
        );

        return PostCreateResponse.of(saved, party.getId(),  lookingPositions, 1, writerDto, participants);
    }

    // 모집글 수정 + 화면용 응답 조립
    @Transactional
    public PostUpdateResponse updatePostWithPartyView(Post post, PostUpdateRequest request) {
        // 합성 검증 포함
        postValidator.validateUpdateMerged(post, request);

        // lookingPositions 변경이 들어오면 JSON으로 직렬화
        String lookingPositionsJson = (request.lookingPositions() != null)
                ? serializePositions(request.lookingPositions())
                : null;

        post.update(
                request.myPosition(),
                lookingPositionsJson,
                request.queueType(),
                request.mic(),
                request.recruitCount(),
                request.memo()
        );

        // 응답 조립 → 단건 조회로 조립 : N+1 이슈가 아니므로 단순 조회로 구성
        User writer = post.getUser(); // 트랜잭션 내
        PostWriter writerDto = buildWriterDto(writer, null, null);

        List<Position> lookingPositions = parsePositions(post.getLookingPositions());

        // 파티가 이미 생성돼 있으면 붙이고, 없으면 작성자만
        Integer currentParticipants = 1;

        // role은 응답 명세서 기준 "LEADER"/"MEMBER" 문자열로 내림
        List<PostParticipant> participants = List.of(
                new PostParticipant(
                        writer.getId(),
                        writer.getNickname(),
                        writer.getProfileImage(),
                        PartyMemberRole.LEADER.name()
                )
        );

        return PostUpdateResponse.of(post, lookingPositions, currentParticipants, writerDto, participants);
    }

    // 목록 조회: 필터, N+1 방지, 응답 조립
    @Transactional(readOnly = true)
    public PostListResponse getPostList(
            Long cursor,
            Integer size,
            PostStatus status,
            QueueType queueType,
            Long gameModeId,
            String myPositionsCsv,
            String tier,
            Long currentUserId
    ) {
        int pageSize = (size == null || size <= 0) ? 20 : Math.min(size, 50);

        // myPositions 파싱 (ANY 포함되면 필터 미적용)
        List<Position> myPositions = postListQueryRepository.parseMyPositionsCsv(myPositionsCsv);

        // tier normalize
        String normalizedTier = (tier == null || tier.isBlank()) ? null : tier.trim().toUpperCase();

        // Post 목록 1번 조회 (size + 1)
        List<Post> posts = postListQueryRepository.findPosts(
                cursor,
                pageSize + 1,
                status,
                queueType,
                gameModeId,
                myPositions.isEmpty() ? null : myPositions,
                normalizedTier
        );

        boolean hasNext = posts.size() > pageSize;
        if (hasNext) {
            posts.remove(posts.size() - 1);
        }

        Long nextCursor = hasNext && !posts.isEmpty()
                ? posts.get(posts.size() - 1).getId()
                : null;

        if (posts.isEmpty()) {
            return new PostListResponse(List.of(), nextCursor, hasNext);
        }

        // writer userIds 수집
        List<Long> writerIds = posts.stream()
                .map(p -> p.getUser().getId())
                .distinct()
                .toList();

        // GameAccount 일괄 조회 (LOL 계정만)
        List<GameAccount> accounts = postGameAccountQueryRepository.findLolAccountsByUserIds(writerIds);
        Map<Long, GameAccount> accountByUserId = accounts.stream()
                .collect(Collectors.toMap(ga -> ga.getUser().getId(), Function.identity(), (a, b) -> a));

        // 4) Rank 일괄 조회 (gameAccountId IN)
        List<Long> gameAccountIds = accounts.stream()
                .map(GameAccount::getGameAccountId)
                .distinct()
                .toList();

        List<Rank> ranks = postGameAccountQueryRepository.findRanksByGameAccountIds(gameAccountIds);

        // 큐타입별 Rank 매칭용 map: gameAccountId -> (queueTypeKey -> Rank)
        Map<Long, Map<String, Rank>> rankMap = new HashMap<>();
        for (Rank r : ranks) {
            Long gaId = r.getGameAccount().getGameAccountId();
            rankMap.computeIfAbsent(gaId, k -> new HashMap<>())
                    .put(r.getQueueType(), r);
        }

        // MatchParticipant 일괄 조회 (솔로랭크 최근 20경기)
        List<MatchParticipant> matchParticipants = 
            postGameAccountQueryRepository.findRecentSoloRankMatchParticipantsByGameAccountIds(gameAccountIds, 20);

        // gameAccountId -> List<MatchParticipant> 매핑
        Map<Long, List<MatchParticipant>> matchesByAccountId = new HashMap<>();
        for (MatchParticipant mp : matchParticipants) {
            Long gaId = mp.getGameAccount().getGameAccountId();
            matchesByAccountId.computeIfAbsent(gaId, k -> new ArrayList<>())
                    .add(mp);
        }

        // 각 계정별 최근 20경기만 유지
        for (Long gaId : matchesByAccountId.keySet()) {
            List<MatchParticipant> matches = matchesByAccountId.get(gaId);
            if (matches.size() > 20) {
                matchesByAccountId.put(gaId, matches.subList(0, 20));
            }
        }

        // FavoriteChampion 일괄 조회
        List<FavoriteChampion> favoriteChampions = 
            postGameAccountQueryRepository.findFavoriteChampionsByGameAccountIds(gameAccountIds);

        // gameAccountId -> List<FavoriteChampion> 매핑
        Map<Long, List<FavoriteChampion>> championsByAccountId = new HashMap<>();
        for (FavoriteChampion fc : favoriteChampions) {
            Long gaId = fc.getGameAccount().getGameAccountId();
            championsByAccountId.computeIfAbsent(gaId, k -> new ArrayList<>())
                    .add(fc);
        }

        // Party 일괄 조회 (postIds IN)
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<Party> parties = postPartyQueryRepository.findPartiesByPostIds(postIds);
        Map<Long, Party> partyByPostId = parties.stream()
                .collect(Collectors.toMap(Party::getPostId, Function.identity(), (a, b) -> a));

        // PartyMember 일괄 조회 (partyIds IN + JOINED + join fetch user)
        List<Long> partyIds = parties.stream().map(Party::getId).toList();
        List<PartyMember> joinedMembers = postPartyQueryRepository.findJoinedMembersByPartyIds(partyIds);

        // partyId -> members
        Map<Long, List<PartyMember>> membersByPartyId = joinedMembers.stream()
                .collect(Collectors.groupingBy(pm -> pm.getParty().getId()));

        // PostDto 조립
        List<PostListResponse.PostDto> dtoList = new ArrayList<>();

        for (Post p : posts) {
            User writer = p.getUser();
            GameAccount ga = accountByUserId.get(writer.getId());

            // writer gameAccount dto
            PostWriter.WriterGameAccount writerGameAccount = null;
            PostWriter.WriterGameSummary writerGameSummary = null;

            if (ga != null) {
                String profileIconUrl = iconUrlBuilder.buildProfileIconUrl(ga.getProfileIconId());
                writerGameAccount = new PostWriter.WriterGameAccount(
                        ga.getGameType(),
                        ga.getGameNickname(),
                        ga.getGameTag(),
                        profileIconUrl
                );

                // 정책: gameMode/queueType 상관없이 솔로랭크 기준 티어만 사용
                Rank matched = findSoloRank(ga.getGameAccountId(), rankMap);

                if (matched != null) {
                    // KDA 계산
                    List<MatchParticipant> matches = matchesByAccountId.getOrDefault(ga.getGameAccountId(), List.of());
                    KdaStats kdaStats = calculateKdaStats(matches);

                    // 선호 챔피언 이미지 URL 생성
                    List<FavoriteChampion> champions = championsByAccountId.getOrDefault(ga.getGameAccountId(), List.of());
                    List<String> championImageUrls = buildChampionImageUrls(champions);

                    writerGameSummary = new PostWriter.WriterGameSummary(
                            matched.getTier(),
                            matched.getRank(),
                            matched.getWinRate(),
                            kdaStats.kda,
                            kdaStats.avgKills,
                            kdaStats.avgDeaths,
                            kdaStats.avgAssists,
                            championImageUrls
                    );
                } else {
                    writerGameSummary = new PostWriter.WriterGameSummary(
                            null, null, null, null, null, null, null, null
                    );
                }
            }

            PostWriter writerDto = new PostWriter(
                    writer.getId(),
                    writer.getNickname(),
                    writer.getProfileImage(),
                    writerGameAccount,
                    writerGameSummary
            );

            // party/participants
            Party party = partyByPostId.get(p.getId());
            List<PostParticipant> participants = new ArrayList<>();
            int currentParticipants = 1;

            if (party != null) {
                List<PartyMember> partyMembers = membersByPartyId.getOrDefault(party.getId(), List.of());
                currentParticipants = partyMembers.size();

                for (PartyMember pm : partyMembers) {
                    User u = pm.getUser();
                    participants.add(new PostParticipant(
                            u.getId(),
                            u.getNickname(),
                            u.getProfileImage(),
                            pm.getRole().name()
                    ));
                }
            } else {
                // 파티가 아직 없으면 작성자만 표시
                currentParticipants = 1;
                participants.add(new PostParticipant(
                        writer.getId(),
                        writer.getNickname(),
                        writer.getProfileImage(),
                        PartyMemberRole.LEADER.name()
                ));
            }

            dtoList.add(new PostListResponse.PostDto(
                    p.getId(),
                    p.getGameMode().getId(),
                    p.getGameMode().getModeCode(),
                    p.getQueueType(),
                    p.getMyPosition(),
                    parsePositions(p.getLookingPositions()),
                    p.getMic(),
                    p.getRecruitCount(),
                    currentParticipants,
                    p.getStatus(),
                    p.getMemo(),
                    p.getCreatedAt(),
                    writerDto,
                    participants
            ));
        }

        return new PostListResponse(dtoList, nextCursor, hasNext);
    }

    // 단건 조회 (전체 데이터로 확장)
    @Transactional(readOnly = true)
    public PostUpdateResponse buildPostDetailForEdit(Post post) {
        User writer = post.getUser();

        // GameAccount 조회
        List<GameAccount> accounts = postGameAccountQueryRepository.findLolAccountsByUserIds(
                List.of(writer.getId())
        );
        GameAccount ga = accounts.isEmpty() ? null : accounts.get(0);

        // Writer DTO 조립
        PostWriter.WriterGameAccount writerGameAccount = null;
        PostWriter.WriterGameSummary writerGameSummary = null;

        if (ga != null) {
            String profileIconUrl = iconUrlBuilder.buildProfileIconUrl(ga.getProfileIconId());
            writerGameAccount = new PostWriter.WriterGameAccount(
                    ga.getGameType(),
                    ga.getGameNickname(),
                    ga.getGameTag(),
                    profileIconUrl
            );

            // Rank 조회
            List<Rank> ranks = postGameAccountQueryRepository.findRanksByGameAccountIds(
                    List.of(ga.getGameAccountId())
            );

            Rank soloRank = ranks.stream()
                    .filter(r -> "RANKED_SOLO_5x5".equals(r.getQueueType()))
                    .findFirst()
                    .orElse(null);

            if (soloRank != null) {
                writerGameSummary = new PostWriter.WriterGameSummary(
                        soloRank.getTier(),
                        soloRank.getRank(),
                        null, null, null, null, null, null
                );
            } else {
                writerGameSummary = new PostWriter.WriterGameSummary(
                        null, null, null, null, null, null, null, null
                );
            }
        }

        PostWriter writerDto = new PostWriter(
                writer.getId(),
                writer.getNickname(),
                writer.getProfileImage(),
                writerGameAccount,
                writerGameSummary
        );

        // Party 조회
        List<Party> parties = postPartyQueryRepository.findPartiesByPostIds(List.of(post.getId()));
        Party party = parties.isEmpty() ? null : parties.get(0);

        List<PostParticipant> participants = new ArrayList<>();
        int currentParticipants = 1;

        if (party != null) {
            List<PartyMember> members = postPartyQueryRepository.findJoinedMembersByPartyIds(
                    List.of(party.getId())
            );
            currentParticipants = members.size();

            for (PartyMember pm : members) {
                User u = pm.getUser();
                participants.add(new PostParticipant(
                        u.getId(),
                        u.getNickname(),
                        u.getProfileImage(),
                        pm.getRole().name()
                ));
            }
        } else {
            participants.add(new PostParticipant(
                    writer.getId(),
                    writer.getNickname(),
                    writer.getProfileImage(),
                    PartyMemberRole.LEADER.name()
            ));
        }

        List<Position> lookingPositions = parsePositions(post.getLookingPositions());

        return PostUpdateResponse.of(post, lookingPositions, currentParticipants, writerDto, participants);
    }

    private Rank findSoloRank(Long gameAccountId, Map<Long, Map<String, Rank>> rankMap) {
        Map<String, Rank> m = rankMap.get(gameAccountId);
        if (m == null) return null;

        // 정책: 솔로랭크만 사용
        return m.get("RANKED_SOLO_5x5");
    }

    private String serializePositions(List<Position> positions) {
        try {
            return objectMapper.writeValueAsString(positions);
        } catch (JsonProcessingException e) {
            throw new CustomException(CustomErrorCode.POSITION_SERIALIZE_FAILED);
        }
    }

    private List<Position> parsePositions(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Position>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private User getUserReference(Long userId) {
        if (userId == null) {
            throw new CustomException(CustomErrorCode.POST_FORBIDDEN);
        }
        if (!userRepository.existsById(userId)) {
            throw new CustomException(CustomErrorCode.POST_FORBIDDEN);
        }
        return entityManager.getReference(User.class, userId);
    }

    private PostWriter buildWriterDto(User writer, PostWriter.WriterGameAccount gameAccount, PostWriter.WriterGameSummary gameSummary) {
        return new PostWriter(
                writer.getId(),
                writer.getNickname(),
                writer.getProfileImage(),
                gameAccount,
                gameSummary
        );
    }

    private static class KdaStats {
        Double kda;
        Double avgKills;
        Double avgDeaths;
        Double avgAssists;

        KdaStats(Double kda, Double avgKills, Double avgDeaths, Double avgAssists) {
            this.kda = kda;
            this.avgKills = avgKills;
            this.avgDeaths = avgDeaths;
            this.avgAssists = avgAssists;
        }
    }

    private KdaStats calculateKdaStats(List<MatchParticipant> matches) {
        if (matches == null || matches.isEmpty()) {
            return new KdaStats(null, null, null, null);
        }

        int totalKills = matches.stream().mapToInt(MatchParticipant::getKills).sum();
        int totalDeaths = matches.stream().mapToInt(MatchParticipant::getDeaths).sum();
        int totalAssists = matches.stream().mapToInt(MatchParticipant::getAssists).sum();

        int gameCount = matches.size();

        double avgKills = (double) totalKills / gameCount;
        double avgDeaths = (double) totalDeaths / gameCount;
        double avgAssists = (double) totalAssists / gameCount;

        double kda = avgDeaths == 0
                ? (avgKills + avgAssists)
                : (avgKills + avgAssists) / avgDeaths;

        kda = Math.round(kda * 100.0) / 100.0;
        avgKills = Math.round(avgKills * 10.0) / 10.0;
        avgDeaths = Math.round(avgDeaths * 10.0) / 10.0;
        avgAssists = Math.round(avgAssists * 10.0) / 10.0;

        return new KdaStats(kda, avgKills, avgDeaths, avgAssists);
    }

    private List<String> buildChampionImageUrls(List<FavoriteChampion> champions) {
        if (champions == null || champions.isEmpty()) {
            return List.of();
        }

        String version = dataDragonService.getLatestVersion();

        return champions.stream()
                .limit(3)
                .map(fc -> String.format(
                        "https://ddragon.leagueoflegends.com/cdn/%s/img/champion/%s.png",
                        version,
                        fc.getChampionName()
                ))
                .collect(Collectors.toList());
    }
}
