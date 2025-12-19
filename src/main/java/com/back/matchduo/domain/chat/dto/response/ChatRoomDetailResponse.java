package com.back.matchduo.domain.chat.dto.response;

import com.back.matchduo.domain.chat.entity.ChatMemberRole;
import com.back.matchduo.domain.chat.entity.ChatRoom;
import com.back.matchduo.domain.gameaccount.entity.GameAccount;
import com.back.matchduo.domain.user.entity.User;

import java.time.LocalDateTime;

/** 채팅방 상세 정보 응답 */
public record ChatRoomDetailResponse(
        Long chatRoomId,
        Long postId,
        boolean isOpen,
        LocalDateTime createdAt,
        ChatMemberRole myRole,
        boolean otherLeft,
        boolean myLeft,
        OtherUserDetailResponse otherUser,
        String queueType,
        String memo,
        String postStatus
) {

    public record OtherUserDetailResponse(
            Long userId,
            String nickname,
            String profileImage,
            String gameNickname,
            String gameTag
    ) {
        public static OtherUserDetailResponse of(User user, GameAccount gameAccount) {
            return new OtherUserDetailResponse(
                    user.getId(),
                    user.getNickname(),
                    user.getProfileImage(),
                    gameAccount != null ? gameAccount.getGameNickname() : null,
                    gameAccount != null ? gameAccount.getGameTag() : null
            );
        }
    }

    public static ChatRoomDetailResponse of(ChatRoom room, Long userId, GameAccount otherGameAccount) {
        ChatMemberRole myRole = room.getMemberRole(userId);
        boolean isSender = myRole == ChatMemberRole.SENDER;
        User otherUser = isSender ? room.getReceiver() : room.getSender();

        return new ChatRoomDetailResponse(
                room.getId(),
                room.getPost().getId(),
                room.isOpen(),
                room.getCreatedAt(),
                myRole,
                isSender ? room.isReceiverLeft() : room.isSenderLeft(),
                isSender ? room.isSenderLeft() : room.isReceiverLeft(),
                OtherUserDetailResponse.of(otherUser, otherGameAccount),
                room.getPost().getQueueType().name(),
                room.getPost().getMemo(),
                room.getPost().getStatus().name()
        );
    }
}
