package com.back.matchduo.global.init;

import com.back.matchduo.domain.chat.entity.ChatMessage;
import com.back.matchduo.domain.chat.entity.ChatRoom;
import com.back.matchduo.domain.chat.entity.MessageType;
import com.back.matchduo.domain.chat.repository.ChatMessageRepository;
import com.back.matchduo.domain.chat.repository.ChatRoomRepository;
import com.back.matchduo.domain.gameaccount.entity.GameAccount;
import com.back.matchduo.domain.gameaccount.repository.GameAccountRepository;
import com.back.matchduo.domain.post.entity.GameMode;
import com.back.matchduo.domain.post.entity.Position;
import com.back.matchduo.domain.post.entity.Post;
import com.back.matchduo.domain.post.entity.QueueType;
import com.back.matchduo.domain.post.repository.PostRepository;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class BaseInitData implements CommandLineRunner {

    private final UserRepository userRepository;
    private final GameAccountRepository gameAccountRepository;
    private final PostRepository postRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    private static final int USER_COUNT = 10;
    private static final int POSTS_PER_USER = 1;
    private static final int CHAT_ROOMS_COUNT = 20;
    private static final int MESSAGES_PER_ROOM = 100;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) {
        log.info("===========================================");
        log.info("부하 테스트용 대량 데이터 초기화 시작...");
        log.info("===========================================");

        // 1. 사용자 생성 (10명)
        List<User> users = createUsers();
        log.info("사용자 {}명 생성 완료", users.size());

        // 2. 게임 계정 생성 (사용자당 1개)
        Map<Long, GameAccount> userGameAccounts = createGameAccounts(users);
        log.info("게임 계정 {}개 생성 완료", userGameAccounts.size());

        // 3. 모집글 생성 (사용자당 1개 = 10개)
        List<Post> posts = createPosts(users, userGameAccounts);
        log.info("모집글 {}개 생성 완료", posts.size());

        // 4. 채팅방 생성 (20개)
        List<ChatRoom> chatRooms = createChatRooms(users, posts);
        log.info("채팅방 {}개 생성 완료", chatRooms.size());

        // 5. 메시지 생성 (채팅방당 100개 = 총 2,000개)
        int messageCount = createMessages(chatRooms);
        log.info("메시지 {}개 생성 완료", messageCount);

        log.info("===========================================");
        log.info("부하 테스트 데이터 초기화 완료!");
        log.info("- 사용자: {}명 (test1@gmail.com ~ test{}@gmail.com)", USER_COUNT, USER_COUNT);
        log.info("- 비밀번호: password123!");
        log.info("- 게임 계정: {}개", userGameAccounts.size());
        log.info("- 모집글: {}개", posts.size());
        log.info("- 채팅방: {}개", chatRooms.size());
        log.info("- 메시지: {}개", messageCount);
        log.info("===========================================");
    }

    private List<User> createUsers() {
        List<User> users = new ArrayList<>();

        for (int i = 1; i <= USER_COUNT; i++) {
            String email = "test" + i + "@gmail.com";
            String nickname = "testuser" + i;

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        User newUser = User.createUser(email, "password123!", nickname);
                        return userRepository.save(newUser);
                    });
            users.add(user);
        }

        return users;
    }

    private Map<Long, GameAccount> createGameAccounts(List<User> users) {
        Map<Long, GameAccount> userGameAccounts = new HashMap<>();

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);

            // 이미 게임 계정이 있는지 확인
            Optional<GameAccount> existingAccount = gameAccountRepository.findByUser_Id(user.getId());
            if (existingAccount.isPresent()) {
                userGameAccounts.put(user.getId(), existingAccount.get());
                continue;
            }

            // 게임 계정 생성
            GameAccount gameAccount = GameAccount.builder()
                    .user(user)
                    .gameNickname("TestPlayer" + (i + 1))
                    .gameTag("KR" + (i + 1))
                    .gameType("LOL")
                    .puuid("test-puuid-" + (i + 1))
                    .profileIconId(1)
                    .build();

            gameAccountRepository.save(gameAccount);
            userGameAccounts.put(user.getId(), gameAccount);
        }

        return userGameAccounts;
    }

    private List<Post> createPosts(List<User> users, Map<Long, GameAccount> userGameAccounts) {
        List<Post> posts = new ArrayList<>();

        // 이미 모집글이 충분히 있으면 스킵
        if (postRepository.count() >= USER_COUNT * POSTS_PER_USER) {
            log.info("모집글이 이미 충분히 존재합니다. 기존 모집글 사용.");
            return postRepository.findAll();
        }

        Position[] positions = Position.values();
        QueueType[] queueTypes = QueueType.values();
        GameMode[] gameModes = {GameMode.SUMMONERS_RIFT, GameMode.HOWLING_ABYSS};

        for (User user : users) {
            GameAccount gameAccount = userGameAccounts.get(user.getId());
            if (gameAccount == null) {
                log.warn("사용자 {}의 게임 계정이 없습니다. 모집글 생성 스킵.", user.getId());
                continue;
            }

            for (int j = 0; j < POSTS_PER_USER; j++) {
                Post post = Post.builder()
                        .user(user)
                        .gameAccount(gameAccount)
                        .gameMode(gameModes[random.nextInt(gameModes.length)])
                        .queueType(queueTypes[random.nextInt(queueTypes.length)])
                        .myPosition(positions[random.nextInt(positions.length)])
                        .lookingPositions("[\"JUNGLE\", \"SUPPORT\", \"MID\"]")
                        .mic(random.nextBoolean())
                        .recruitCount(random.nextInt(4) + 1)
                        .memo("테스트 모집글 #" + (posts.size() + 1) + " - " + user.getNickname())
                        .build();
                posts.add(postRepository.save(post));
            }
        }

        return posts;
    }

    private List<ChatRoom> createChatRooms(List<User> users, List<Post> posts) {
        List<ChatRoom> chatRooms = new ArrayList<>();

        // 이미 채팅방이 충분히 있으면 스킵
        if (chatRoomRepository.count() >= CHAT_ROOMS_COUNT) {
            log.info("채팅방이 이미 충분히 존재합니다. 기존 채팅방 사용.");
            return chatRoomRepository.findAll();
        }

        int roomCount = 0;
        for (Post post : posts) {
            if (roomCount >= CHAT_ROOMS_COUNT) break;

            User receiver = post.getUser(); // 모집글 작성자 = 방장

            for (User sender : users) {
                if (roomCount >= CHAT_ROOMS_COUNT) break;
                if (sender.getId().equals(receiver.getId())) continue; // 본인 제외

                // 이미 존재하는 채팅방인지 확인
                boolean exists = chatRoomRepository.findByPostIdAndSenderId(post.getId(), sender.getId()).isPresent();
                if (exists) continue;

                ChatRoom chatRoom = ChatRoom.create(post, receiver, sender);
                chatRooms.add(chatRoomRepository.save(chatRoom));
                roomCount++;
            }
        }

        return chatRooms;
    }

    private int createMessages(List<ChatRoom> chatRooms) {
        int totalMessages = 0;

        String[] sampleMessages = {
                "안녕하세요! 듀오 하실래요?",
                "네 반갑습니다!",
                "지금 게임 가능하신가요?",
                "잠시만요 준비중입니다",
                "포지션 어디 하시나요?",
                "저는 미드 메인이에요",
                "오 저는 정글이요",
                "ㄱㄱ",
                "좋아요 시작합시다",
                "게임 끝나면 또 해요!",
                "GG",
                "다음에 또 하실래요?",
                "네 좋아요!",
                "티어가 어떻게 되세요?",
                "골드입니다",
                "저도 골드에요 ㅋㅋ",
                "마이크 있으신가요?",
                "네 디코 가능해요",
                "초대 보낼게요",
                "확인했습니다!"
        };

        for (ChatRoom chatRoom : chatRooms) {
            // 이미 메시지가 있으면 스킵
            long existingMessages = chatMessageRepository.countByChatRoomId(chatRoom.getId());
            if (existingMessages >= MESSAGES_PER_ROOM) {
                totalMessages += (int) existingMessages;
                continue;
            }

            User sender = chatRoom.getSender();
            User receiver = chatRoom.getReceiver();

            List<ChatMessage> messages = new ArrayList<>();
            for (int i = 0; i < MESSAGES_PER_ROOM; i++) {
                // 번갈아가며 메시지 전송
                User messageSender = (i % 2 == 0) ? sender : receiver;
                String content = sampleMessages[random.nextInt(sampleMessages.length)] + " (" + (i + 1) + ")";

                ChatMessage message = ChatMessage.create(chatRoom, messageSender, MessageType.TEXT, content);
                messages.add(message);
            }

            chatMessageRepository.saveAll(messages);
            totalMessages += messages.size();
        }

        return totalMessages;
    }
}