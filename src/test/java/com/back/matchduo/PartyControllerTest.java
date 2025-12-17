package com.back.matchduo;

import com.back.matchduo.domain.post.entity.GameMode;
import com.back.matchduo.domain.post.repository.GameModeRepository;
import com.back.matchduo.domain.party.controller.PartyController;
import com.back.matchduo.domain.party.dto.request.PartyMemberAddRequest;
import com.back.matchduo.domain.party.entity.Party;
import com.back.matchduo.domain.party.entity.PartyMember;
import com.back.matchduo.domain.party.entity.PartyMemberRole;
import com.back.matchduo.domain.party.repository.PartyMemberRepository;
import com.back.matchduo.domain.party.repository.PartyRepository;
import com.back.matchduo.domain.post.entity.Position;
import com.back.matchduo.domain.post.entity.Post;
import com.back.matchduo.domain.post.entity.QueueType;
import com.back.matchduo.domain.post.repository.PostRepository;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.repository.UserRepository;
import com.back.matchduo.global.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PartyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private PartyMemberRepository partyMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private GameModeRepository gameModeRepository;

    private Long testPostId;
    private User leaderUser;
    private User memberUser;
    private Party testParty;
    private User targetUser1;
    private User targetUser2;
    private PartyMember leaderMember;
    private PartyMember normalMember;

    @BeforeAll
    void setUp() {
        // 1. 유저 생성 (파티장)
        leaderUser = User.builder()
                .email("leader@test.com")
                .password("1234")
                .nickname("파티장")
                .verification_code("1234")
                .build();
        userRepository.save(leaderUser);

        // 2. 유저 생성 (일반 멤버)
        memberUser = User.builder()
                .email("member@test.com")
                .password("1234")
                .nickname("파티원")
                .verification_code("5678")
                .build();
        userRepository.save(memberUser);

        // 3. 모집글(Post) 생성을 위한 GameMode 저장
        GameMode gameMode = new GameMode("SOLO_RANK", "솔로랭크", true);
        gameModeRepository.save(gameMode);

        // 4. 모집글(Post) 생성 및 저장
        Post post = Post.builder()
                .user(leaderUser)
                .gameMode(gameMode)
                .queueType(QueueType.DUO) // 제공해주신 Enum에 있는 값(DUO) 사용
                .myPosition(Position.TOP)
                .lookingPositions("[\"JUNGLE\"]")
                .mic(true)
                .recruitCount(2)
                .memo("테스트 모집글")
                .build();
        postRepository.save(post);
        testPostId = post.getId();

        // 5. 파티 생성
        testParty = new Party(testPostId, leaderUser.getId());
        partyRepository.save(testParty);

        // 6. 멤버 추가 (User 객체를 직접 전달)

        leaderMember = new PartyMember(testParty, leaderUser, PartyMemberRole.LEADER);
        partyMemberRepository.save(leaderMember);

        normalMember = new PartyMember(testParty, memberUser, PartyMemberRole.MEMBER);
        partyMemberRepository.save(normalMember);

        // 7. 초대 대상 유저 생성
        targetUser1 = User.builder()
                .email("target1@test.com").password("1234").nickname("초대대상1").verification_code("0000").build();
        userRepository.save(targetUser1);

        targetUser2 = User.builder()
                .email("target2@test.com").password("1234").nickname("초대대상2").verification_code("0000").build();
        userRepository.save(targetUser2);
    }

    @Nested
    @DisplayName("모집글 기준 파티 조회 API")
    class GetPartyByPost {

        @Test
        @DisplayName("성공: 파티 정보와 멤버 목록 조회 (로그인 상태)")
        void success() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/posts/{postId}/party", testPostId)
                            .accept(MediaType.APPLICATION_JSON)
                            .with(user(new CustomUserDetails(leaderUser)))
            );

            // then
            resultActions
                    .andExpect(handler().handlerType(PartyController.class))
                    .andExpect(handler().methodName("getPartyByPost"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.postId").value(testPostId))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.currentCount").value(2))
                    .andExpect(jsonPath("$.maxCount").value(2))
                    .andExpect(jsonPath("$.isJoined").value(true))
                    .andDo(print());
        }

        @Test
        @DisplayName("성공: 비로그인 상태로 조회 (isJoined = false 확인)")
        void success_guest() throws Exception {
            // given
            // 로그인 정보 없이 요청

            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/posts/{postId}/party", testPostId)
                            .accept(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.postId").value(testPostId))
                    .andExpect(jsonPath("$.isJoined").value(false))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 파티 조회")
        void fail_party_not_found() throws Exception {
            // given
            Long invalidPostId = 9999L;

            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/posts/{postId}/party", invalidPostId)
                            .accept(MediaType.APPLICATION_JSON)
                            .with(user(new CustomUserDetails(leaderUser)))
            );

            // then
            resultActions
                    .andExpect(status().isNotFound()) // 404 Not Found
                    .andExpect(jsonPath("$.code").value("PARTY_NOT_FOUND"))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("파티원 초대/추가 API")
    class AddPartyMember {

        @Test
        @DisplayName("성공: 파티장이 유저 2명을 초대하면 멤버 목록에 추가된다")
        void success_invite_multiple_users() throws Exception {
            // given
            List<Long> targetIds = List.of(targetUser1.getId(), targetUser2.getId());
            PartyMemberAddRequest request = new PartyMemberAddRequest(targetIds);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/parties/{partyId}/members", testParty.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            // ★ 핵심: 파티장(leaderUser) 권한으로 요청
                            .with(user(new CustomUserDetails(leaderUser)))
            );

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(2))
                    .andExpect(jsonPath("$[0].userId").value(targetUser1.getId()))
                    .andExpect(jsonPath("$[0].role").value("MEMBER"))
                    .andExpect(jsonPath("$[1].userId").value(targetUser2.getId()))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 파티장이 아닌 유저가 초대를 시도하면 403 Forbidden")
        void fail_not_leader() throws Exception {
            // given
            List<Long> targetIds = List.of(targetUser1.getId());
            PartyMemberAddRequest request = new PartyMemberAddRequest(targetIds);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/parties/{partyId}/members", testParty.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(new CustomUserDetails(memberUser)))
            );

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("NOT_PARTY_LEADER")) // CustomErrorCode 확인
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 초대할 유저를 선택하지 않음 (빈 리스트)")
        void fail_empty_list() throws Exception {
            // given
            PartyMemberAddRequest request = new PartyMemberAddRequest(List.of());

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/parties/{partyId}/members", testParty.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(new CustomUserDetails(leaderUser)))
            );

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("파티원 제외(강퇴) API")
    class RemovePartyMember {

        @Test
        @DisplayName("성공: 파티장이 멤버를 강퇴하면 상태가 LEFT로 변경된다")
        void success_kick_member() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(
                    delete("/api/v1/parties/{partyId}/members/{memberId}", testParty.getId(), normalMember.getId())
                            .accept(MediaType.APPLICATION_JSON)
                            .with(user(new CustomUserDetails(leaderUser)))
            );

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.message").value("파티원이 제외되었습니다."))
                    .andExpect(jsonPath("$.data.partyMemberId").value(normalMember.getId()))
                    .andExpect(jsonPath("$.data.state").value("LEFT"))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 파티장이 아닌 유저가 강퇴를 시도하면 403 Forbidden")
        void fail_not_leader() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(
                    delete("/api/v1/parties/{partyId}/members/{memberId}", testParty.getId(), normalMember.getId())
                            .accept(MediaType.APPLICATION_JSON)
                            .with(user(new CustomUserDetails(memberUser))) // 일반 유저 권한
            );

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("NOT_PARTY_LEADER"))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 다른 파티의 멤버를 강퇴하려 하면 400 Bad Request")
        void fail_member_mismatch() throws Exception {
            // given
            Party otherParty = new Party(200L, targetUser1.getId());
            partyRepository.save(otherParty);

            // [수정] PartyMember 생성 시 User 객체 전달 (targetUser2)
            PartyMember otherPartyMember = new PartyMember(otherParty, targetUser2, PartyMemberRole.MEMBER);
            partyMemberRepository.save(otherPartyMember);

            // when
            ResultActions resultActions = mockMvc.perform(
                    delete("/api/v1/parties/{partyId}/members/{memberId}", testParty.getId(), otherPartyMember.getId())
                            .accept(MediaType.APPLICATION_JSON)
                            .with(user(new CustomUserDetails(leaderUser)))
            );

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("PARTY_MEMBER_NOT_MATCH"))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 파티장이 자기 자신을 강퇴하려 하면 400 Bad Request")
        void fail_kick_self() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(
                    delete("/api/v1/parties/{partyId}/members/{memberId}", testParty.getId(), leaderMember.getId())
                            .accept(MediaType.APPLICATION_JSON)
                            .with(user(new CustomUserDetails(leaderUser)))
            );

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("CANNOT_KICK_LEADER"))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("파티원 목록 조회 API")
    class GetPartyMemberList {

        @Test
        @DisplayName("성공: 로그인 없이 파티원 목록과 정원 정보를 조회한다")
        void success() throws Exception {

            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/parties/{partyId}/members", testParty.getId())
                            .accept(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("파티원 목록을 조회했습니다."))
                    // 1. 파티 정보 및 인원수 검증
                    .andExpect(jsonPath("$.data.partyId").value(testParty.getId()))
                    .andExpect(jsonPath("$.data.currentCount").value(2))
                    .andExpect(jsonPath("$.data.maxCount").value(2))

                    // 2. 멤버 리스트 검증
                    .andExpect(jsonPath("$.data.members.size()").value(2))

                    // 첫 번째 멤버 (파티장 - 먼저 save됨)
                    .andExpect(jsonPath("$.data.members[0].userId").value(leaderUser.getId()))
                    .andExpect(jsonPath("$.data.members[0].role").value("LEADER"))
                    .andExpect(jsonPath("$.data.members[0].nickname").value(leaderUser.getNickname()))

                    // 두 번째 멤버 (일반 파티원)
                    .andExpect(jsonPath("$.data.members[1].userId").value(memberUser.getId()))
                    .andExpect(jsonPath("$.data.members[1].role").value("MEMBER"))
                    .andExpect(jsonPath("$.data.members[1].nickname").value(memberUser.getNickname()))

                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 파티 ID로 조회 시 404 에러")
        void fail_party_not_found() throws Exception {
            // given
            Long invalidPartyId = 99999L;

            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/parties/{partyId}/members", invalidPartyId)
                            .accept(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PARTY_NOT_FOUND"))
                    .andDo(print());
        }
    }
}