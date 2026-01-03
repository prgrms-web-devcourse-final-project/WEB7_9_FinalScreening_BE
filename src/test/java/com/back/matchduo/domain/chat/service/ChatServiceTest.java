package com.back.matchduo.domain.chat.service;

import com.back.matchduo.domain.chat.dto.internal.ChatMessagesWithRoom;
import com.back.matchduo.domain.chat.dto.internal.ChatRoomDetailWithGameAccount;
import com.back.matchduo.domain.chat.entity.ChatMessage;
import com.back.matchduo.domain.chat.entity.ChatMessageRead;
import com.back.matchduo.domain.chat.entity.ChatRoom;
import com.back.matchduo.domain.chat.entity.MessageType;
import com.back.matchduo.domain.gameaccount.entity.GameAccount;
import com.back.matchduo.domain.gameaccount.repository.GameAccountRepository;
import com.back.matchduo.domain.post.entity.GameMode;
import com.back.matchduo.domain.post.entity.Position;
import com.back.matchduo.domain.post.entity.Post;
import com.back.matchduo.domain.post.entity.QueueType;
import com.back.matchduo.domain.post.repository.PostRepository;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.repository.UserRepository;
import com.back.matchduo.global.exeption.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ChatServiceTest {

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private GameAccountRepository gameAccountRepository;

    private User postAuthor;
    private User applicant;
    private GameAccount gameAccount;
    private Post testPost;

    @BeforeEach
    void setUp() {
        postAuthor = userRepository.save(User.builder()
                .email("author@test.com")
                .password("password123")
                .nickname("작성자")
                .verificationCode("1234")
                .build());

        applicant = userRepository.save(User.builder()
                .email("applicant@test.com")
                .password("password123")
                .nickname("지원자")
                .verificationCode("5678")
                .build());

        gameAccount = gameAccountRepository.save(GameAccount.builder()
                .gameNickname("테스트게임닉네임")
                .gameTag("KR1")
                .gameType("LOL")
                .user(postAuthor)
                .build());

        testPost = postRepository.save(Post.builder()
                .user(postAuthor)
                .gameAccount(gameAccount)
                .gameMode(GameMode.SUMMONERS_RIFT)
                .queueType(QueueType.DUO)
                .myPosition(Position.MID)
                .lookingPositions("[\"TOP\", \"JUNGLE\"]")
                .mic(true)
                .recruitCount(1)
                .memo("테스트 모집글")
                .build());
    }

    @Nested
    @DisplayName("채팅방 서비스")
    class ChatRoomServiceTest {

        @Test
        @DisplayName("모집글을 통한 채팅방 생성 성공")
        void createOrGet_success() {
            // when
            ChatRoom room = chatRoomService.createOrGet(testPost.getId(), applicant.getId());

            // then
            assertThat(room).isNotNull();
            assertThat(room.getId()).isNotNull();
            assertThat(room.getSender().getId()).isEqualTo(applicant.getId());
            assertThat(room.getReceiver().getId()).isEqualTo(postAuthor.getId());
        }

        @Test
        @DisplayName("채팅방 생성 멱등성 - 같은 요청 시 동일 채팅방 반환")
        void createOrGet_idempotent() {
            // given
            ChatRoom room1 = chatRoomService.createOrGet(testPost.getId(), applicant.getId());

            // when
            ChatRoom room2 = chatRoomService.createOrGet(testPost.getId(), applicant.getId());

            // then
            assertThat(room1.getId()).isEqualTo(room2.getId());
        }

        @Test
        @DisplayName("본인에게 채팅 시도 시 실패")
        void createOrGet_fail_self_chat() {
            assertThatThrownBy(() -> chatRoomService.createOrGet(testPost.getId(), postAuthor.getId()))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        @DisplayName("채팅방 나가기 성공")
        void leave_success() {
            // given
            ChatRoom room = chatRoomService.createOrGet(testPost.getId(), applicant.getId());

            // when
            ChatRoom leftRoom = chatRoomService.leave(room.getId(), applicant.getId());

            // then
            assertThat(leftRoom.isClosed()).isTrue();
            assertThat(leftRoom.isSenderLeft()).isTrue();
        }

        @Test
        @DisplayName("채팅방 조회 성공")
        void getRoomWithGameAccount_success() {
            // given
            ChatRoom room = chatRoomService.createOrGet(testPost.getId(), applicant.getId());

            // when
            ChatRoomDetailWithGameAccount result = chatRoomService.getRoomWithGameAccount(room.getId(), applicant.getId());

            // then
            assertThat(result.room().getId()).isEqualTo(room.getId());
        }

        @Test
        @DisplayName("참여하지 않은 사용자 채팅방 조회 실패")
        void getRoomWithGameAccount_fail_not_member() {
            // given
            ChatRoom room = chatRoomService.createOrGet(testPost.getId(), applicant.getId());

            User stranger = userRepository.save(User.builder()
                    .email("stranger@test.com")
                    .password("password123")
                    .nickname("타인")
                    .verificationCode("9999")
                    .build());

            // when & then
            assertThatThrownBy(() -> chatRoomService.getRoomWithGameAccount(room.getId(), stranger.getId()))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    @DisplayName("메시지 서비스")
    class ChatMessageServiceTest {

        private ChatRoom chatRoom;

        @BeforeEach
        void setUpChatRoom() {
            chatRoom = chatRoomService.createOrGet(testPost.getId(), applicant.getId());
        }

        @Test
        @DisplayName("메시지 전송 성공")
        void send_success() {
            // when
            ChatMessage message = chatMessageService.send(
                    chatRoom.getId(), applicant.getId(), MessageType.TEXT, "안녕하세요!");

            // then
            assertThat(message).isNotNull();
            assertThat(message.getId()).isNotNull();
            assertThat(message.getContent()).isEqualTo("안녕하세요!");
        }

        @Test
        @DisplayName("닫힌 채팅방에 메시지 전송 실패")
        void send_fail_closed_room() {
            // given
            chatRoomService.leave(chatRoom.getId(), applicant.getId());

            // when & then
            assertThatThrownBy(() -> chatMessageService.send(
                    chatRoom.getId(), postAuthor.getId(), MessageType.TEXT, "메시지"))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        @DisplayName("메시지 목록 조회 성공")
        void getMessagesWithRoom_success() {
            // given
            chatMessageService.send(chatRoom.getId(), applicant.getId(), MessageType.TEXT, "메시지1");
            chatMessageService.send(chatRoom.getId(), postAuthor.getId(), MessageType.TEXT, "메시지2");

            // when
            ChatMessagesWithRoom result = chatMessageService.getMessagesWithRoom(
                    chatRoom.getId(), applicant.getId(), null, 10);

            // then
            assertThat(result.messages()).hasSize(2);
            assertThat(result.room().getId()).isEqualTo(chatRoom.getId());
        }

        @Test
        @DisplayName("메시지 읽음 처리 성공")
        void markReadUpTo_success() {
            // given
            ChatMessage message = chatMessageService.send(
                    chatRoom.getId(), applicant.getId(), MessageType.TEXT, "메시지");

            // when
            ChatMessageRead readState = chatMessageService.markReadUpTo(
                    chatRoom.getId(), postAuthor.getId(), message.getId());

            // then
            assertThat(readState.getLastReadMessage().getId()).isEqualTo(message.getId());
        }
    }
}
