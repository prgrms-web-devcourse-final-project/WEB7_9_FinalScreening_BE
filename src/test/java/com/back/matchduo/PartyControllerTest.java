package com.back.matchduo;

import com.back.matchduo.domain.party.controller.PartyController;
import com.back.matchduo.domain.party.dto.response.PartyByPostResponse;
import com.back.matchduo.domain.party.entity.Party;
import com.back.matchduo.domain.party.entity.PartyMember;
import com.back.matchduo.domain.party.entity.PartyMemberRole;
import com.back.matchduo.domain.party.repository.PartyMemberRepository;
import com.back.matchduo.domain.party.repository.PartyRepository;
import com.back.matchduo.domain.user.entity.User; // ★ 중요: 도메인 User 엔티티 임포트
import com.back.matchduo.domain.user.repository.UserRepository;
import com.back.matchduo.global.security.CustomUserDetails; // ★ 중요: 우리가 만든 UserDetails
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

// MockMvc RequestPostProcessor 임포트 (인증 객체 주입용)
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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


    private Long testPostId = 100L;
    private User leaderUser;
    private User memberUser;
    private Party testParty;

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

        // 3. 파티 생성 (모집글 ID: 100)
        testParty = new Party(testPostId, leaderUser.getId());
        partyRepository.save(testParty);

        PartyMember leaderMember = new PartyMember(testParty, leaderUser.getId(), PartyMemberRole.LEADER);
        partyMemberRepository.save(leaderMember);

        PartyMember normalMember = new PartyMember(testParty, memberUser.getId(), PartyMemberRole.MEMBER);
        partyMemberRepository.save(normalMember);
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
}