package com.back.matchduo.domain.chat.controller;

import com.back.matchduo.domain.chat.dto.internal.ChatMessagesWithRoom;
import com.back.matchduo.domain.chat.dto.internal.ChatRoomDetailWithGameAccount;
import com.back.matchduo.domain.chat.dto.request.ChatMessageReadRequest;
import com.back.matchduo.domain.chat.dto.request.ChatMessageSendRequest;
import com.back.matchduo.domain.chat.dto.request.ChatRoomCreateRequest;
import com.back.matchduo.domain.chat.dto.response.ChatRoomSummaryResponse;
import com.back.matchduo.domain.chat.entity.ChatMessage;
import com.back.matchduo.domain.chat.entity.ChatMessageRead;
import com.back.matchduo.domain.chat.entity.ChatRoom;
import com.back.matchduo.domain.chat.entity.MessageType;
import com.back.matchduo.domain.chat.service.ChatMessageService;
import com.back.matchduo.domain.chat.service.ChatRoomService;
import com.back.matchduo.domain.post.entity.GameMode;
import com.back.matchduo.domain.post.entity.Post;
import com.back.matchduo.domain.post.entity.PostStatus;
import com.back.matchduo.domain.post.entity.QueueType;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.global.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("removal")
@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private ChatRoomService chatRoomService;
    @MockitoBean private ChatMessageService chatMessageService;

    private User postAuthor;
    private User applicant;
    private Post testPost;
    private ChatRoom chatRoom;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        postAuthor = User.builder()
                .email("author@test.com")
                .password("password123")
                .nickname("작성자")
                .verificationCode("1234")
                .build();
        ReflectionTestUtils.setField(postAuthor, "id", 1L);

        applicant = User.builder()
                .email("applicant@test.com")
                .password("password123")
                .nickname("지원자")
                .verificationCode("5678")
                .build();
        ReflectionTestUtils.setField(applicant, "id", 2L);

        testPost = mock(Post.class);
        GameMode gameMode = mock(GameMode.class);
        QueueType queueType = QueueType.DUO;
        given(gameMode.getName()).willReturn("솔로 랭크");
        given(testPost.getId()).willReturn(100L);
        given(testPost.getUser()).willReturn(postAuthor);
        given(testPost.getGameMode()).willReturn(gameMode);
        given(testPost.getQueueType()).willReturn(queueType);
        given(testPost.getStatus()).willReturn(PostStatus.RECRUITING);
        given(testPost.getMemo()).willReturn("테스트 메모");


        chatRoom = ChatRoom.create(testPost, postAuthor, applicant);
        ReflectionTestUtils.setField(chatRoom, "id", 1L);

        userDetails = new CustomUserDetails(applicant);
    }

    @Test
    @DisplayName("채팅방 생성 API 성공")
    void createChatRoom_success() throws Exception {
        // given
        ChatRoomCreateRequest request = new ChatRoomCreateRequest(100L);
        given(chatRoomService.createOrGet(100L, 2L)).willReturn(chatRoom);

        // when & then
        mockMvc.perform(
                        post("/api/v1/chats")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf())
                                .with(user(userDetails))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chatRoomId").value(1L))
                .andExpect(jsonPath("$.postId").value(100L));
    }

    @Test
    @DisplayName("채팅방 목록 조회 API 성공")
    void getChatRoomList_success() throws Exception {
        // given
        ChatRoomSummaryResponse summary = new ChatRoomSummaryResponse(
                1L,
                100L,
                new ChatRoomSummaryResponse.OtherUserResponse(1L, "작성자", null),
                null,
                0,
                "DUO",
                "테스트 메모",
                true,
                null
        );

        given(chatRoomService.getMyRoomsWithSummary(2L, null, 21))
                .willReturn(List.of(summary));

        // when & then
        mockMvc.perform(
                        get("/api/v1/chats")
                                .param("size", "20")
                                .with(user(userDetails))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatRooms").isArray())
                .andExpect(jsonPath("$.chatRooms[0].chatRoomId").value(1L));
    }

    @Test
    @DisplayName("메시지 전송 API 성공")
    void sendMessage_success() throws Exception {
        // given
        ChatMessageSendRequest request = new ChatMessageSendRequest(MessageType.TEXT, "안녕하세요!");

        ChatMessage message = ChatMessage.create(chatRoom, applicant, MessageType.TEXT, "안녕하세요!");
        ReflectionTestUtils.setField(message, "id", 1L);
        ReflectionTestUtils.setField(message, "createdAt", LocalDateTime.now());

        given(chatMessageService.send(1L, 2L, MessageType.TEXT, "안녕하세요!"))
                .willReturn(message);

        // when & then
        mockMvc.perform(
                        post("/api/v1/chats/{chatId}/messages", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf())
                                .with(user(userDetails))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chatMessageId").value(1L))
                .andExpect(jsonPath("$.content").value("안녕하세요!"));
    }

    @Test
    @DisplayName("메시지 목록 조회 API 성공")
    void getMessages_success() throws Exception {
        // given
        ChatMessage message = ChatMessage.create(chatRoom, applicant, MessageType.TEXT, "테스트 메시지");
        ReflectionTestUtils.setField(message, "id", 1L);
        ReflectionTestUtils.setField(message, "createdAt", LocalDateTime.now());

        ChatMessagesWithRoom result = new ChatMessagesWithRoom(List.of(message), chatRoom);

        given(chatMessageService.getMessagesWithRoom(1L, 2L, null, 31))
                .willReturn(result);

        // when & then
        mockMvc.perform(
                        get("/api/v1/chats/{chatId}/messages", 1L)
                                .param("size", "30")
                                .with(user(userDetails))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatRoomId").value(1L))
                .andExpect(jsonPath("$.messages[0].content").value("테스트 메시지"));
    }

    @Test
    @DisplayName("채팅방 상세 조회 API 성공")
    void getChatRoom_success() throws Exception {
        // given
        ChatRoomDetailWithGameAccount result = new ChatRoomDetailWithGameAccount(chatRoom, null);
        given(chatRoomService.getRoomWithGameAccount(1L, 2L)).willReturn(result);

        // when & then
        mockMvc.perform(
                        get("/api/v1/chats/{chatId}", 1L)
                                .with(user(userDetails))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatRoomId").value(1L))
                .andExpect(jsonPath("$.otherUser.userId").value(1L))
                .andExpect(jsonPath("$.otherUser.nickname").value("작성자"));
    }

    @Test
    @DisplayName("채팅방 나가기 API 성공")
    void leaveRoom_success() throws Exception {
        // given - senderLeft=true이면 isClosed()가 true 반환
        ReflectionTestUtils.setField(chatRoom, "senderLeft", true);
        given(chatRoomService.leave(1L, 2L)).willReturn(chatRoom);

        // when & then
        mockMvc.perform(
                        delete("/api/v1/chats/{chatId}", 1L)
                                .with(csrf())
                                .with(user(userDetails))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatRoomId").value(1L))
                .andExpect(jsonPath("$.isClosed").value(true));
    }

    @Test
    @DisplayName("메시지 읽음 처리 API 성공")
    void markAsRead_success() throws Exception {
        // given
        ChatMessageReadRequest request = new ChatMessageReadRequest(10L);

        ChatMessage lastReadMessage = ChatMessage.create(chatRoom, postAuthor, MessageType.TEXT, "마지막 읽은 메시지");
        ReflectionTestUtils.setField(lastReadMessage, "id", 10L);

        ChatMessageRead readState = ChatMessageRead.create(chatRoom, applicant);
        ReflectionTestUtils.setField(readState, "lastReadMessage", lastReadMessage);
        ReflectionTestUtils.setField(readState, "lastReadAt", LocalDateTime.now());

        given(chatMessageService.markReadUpTo(1L, 2L, 10L)).willReturn(readState);

        // when & then
        mockMvc.perform(
                        post("/api/v1/chats/{chatId}/messages/read", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf())
                                .with(user(userDetails))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatRoomId").value(1L))
                .andExpect(jsonPath("$.lastReadMessageId").value(10L));
    }

}