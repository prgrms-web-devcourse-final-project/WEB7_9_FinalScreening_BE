package com.back.matchduo.domain.party.service;

import com.back.matchduo.domain.chat.repository.ChatRoomRepository;
import com.back.matchduo.domain.party.dto.request.PartyMemberAddRequest;
import com.back.matchduo.domain.party.dto.response.*;
import com.back.matchduo.domain.party.entity.*;
import com.back.matchduo.domain.party.repository.PartyMemberRepository;
import com.back.matchduo.domain.party.repository.PartyRepository;
import com.back.matchduo.domain.post.entity.Post;
import com.back.matchduo.domain.post.entity.PostStatus;
import com.back.matchduo.domain.post.repository.PostRepository;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.repository.UserRepository;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyService {

    private final PartyRepository partyRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ChatRoomRepository chatRoomRepository;


    public PartyByPostResponse getPartyByPostId(Long postId, Long currentUserId) {
        // 1. 파티 정보 조회
        Party party = partyRepository.findByPostId(postId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PARTY_NOT_FOUND));

        // 2. 파티에 속한 모든 멤버 조회
        List<PartyMember> allMembers = partyMemberRepository.findByPartyId(party.getId());

        // 3. 현재 유저의 참여 여부 확인
        boolean isJoined = false;
        if (currentUserId != null) {
            for (PartyMember member : allMembers) {
                if (member.getUser().getId().equals(currentUserId) && member.getState() == PartyMemberState.JOINED) {
                    isJoined = true;
                    break;
                }
            }
        }
        // 4. 멤버 목록 DTO 변환
        List<PartyByPostResponse.PartyMemberDto> memberDtos = new ArrayList<>();
        for (PartyMember member : allMembers) {
            if (member.getState() != PartyMemberState.JOINED) {
                continue;
            }
            User user = member.getUser();
            String nickname = user.getNickname();
            String profileImage = (user.getProfileImage() != null)
                    ? user.getProfileImage()
                    : "https://opgg-static.akamaized.net/meta/images/profile_icons/profileIcon1.jpg";

            PartyByPostResponse.PartyMemberDto dto = PartyByPostResponse.PartyMemberDto.of(
                    member.getId(),
                    user.getId(),
                    nickname,
                    profileImage,
                    member.getRole()
            );
            memberDtos.add(dto);
        }

        // 5. 모집글 정보 조회
        Post post = postRepository.findById(party.getPostId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.POST_NOT_FOUND));

        // 6. 응답 반환
        return new PartyByPostResponse(
                party.getId(),
                party.getPostId(),
                party.getStatus(),
                memberDtos.size(),
                post.getRecruitCount(),
                party.getCreatedAt(),
                isJoined,
                memberDtos
        );
    }
    @Transactional(readOnly = true)
    public List<ChatCandidateResponse> getChatCandidates(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(currentUserId)) {
            throw new CustomException(CustomErrorCode.NOT_PARTY_LEADER);
        }

        List<User> candidates = chatRoomRepository.findCandidateUsers(postId, currentUserId);

        // 3. DTO 변환
        return candidates.stream()
                .map(ChatCandidateResponse::from)
                .collect(Collectors.toList());
    }

    // 파티원 추가
    @Transactional
    public List<PartyMemberAddResponse> addMembers(Long partyId, Long currentUserId, PartyMemberAddRequest request) {
        // 1. 파티 조회
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PARTY_NOT_FOUND));

        // 2. 권한 검증
        if (!party.getLeaderId().equals(currentUserId)) {
            throw new CustomException(CustomErrorCode.NOT_PARTY_LEADER);
        }

        // 3. 상태 검증 (CLOSED,ACTIVE 이면 추가 시도 불가하도록)
        if (party.getStatus() == PartyStatus.CLOSED || party.getStatus() == PartyStatus.ACTIVE) {
            throw new CustomException(CustomErrorCode.PARTY_ALREADY_CLOSED);
        }

        List<PartyMemberAddResponse> responses = new ArrayList<>();

        for (Long targetUserId : request.targetUserIds()) {
            Optional<PartyMember> existingMemberOpt = partyMemberRepository.findByPartyIdAndUserId(partyId, targetUserId);

            if (existingMemberOpt.isPresent()) {
                PartyMember existingMember = existingMemberOpt.get();
                if (existingMember.getState() == PartyMemberState.JOINED) {
                    throw new CustomException(CustomErrorCode.PARTY_ALREADY_JOINED);
                }
                existingMember.rejoinParty();
                responses.add(createAddResponse(existingMember));
            } else {
                User targetUser = userRepository.findById(targetUserId)
                        .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));
                PartyMember newMember = new PartyMember(party, targetUser, PartyMemberRole.MEMBER);
                partyMemberRepository.save(newMember);
                responses.add(createAddResponse(newMember));
            }
        }

        // 4. 인원 수 체크 및 상태 변경 (RECRUIT -> ACTIVE)
        int currentCount = partyMemberRepository.countByPartyIdAndState(partyId, PartyMemberState.JOINED);

        // 모집 정원 확인
        Post post = postRepository.findById(party.getPostId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.POST_NOT_FOUND));

        // 정원이 꽉 찼고, 현재 상태가 '모집 중(RECRUIT)'이라면 -> ACTIVE로 변경 및 6시간 타이머 설정
        if (currentCount >= post.getRecruitCount()) {
            if (party.getStatus() == PartyStatus.RECRUIT) {
                party.activateParty(LocalDateTime.now().plusHours(6));
            }
        }

        return responses;
    }

    private PartyMemberAddResponse createAddResponse(PartyMember member) {
        User user = member.getUser();
        String nickname = user.getNickname();
        String profileImage = (user.getProfileImage() != null)
                ? user.getProfileImage()
                : "https://opgg-static.akamaized.net/meta/images/profile_icons/profileIcon1.jpg";
        return PartyMemberAddResponse.of(member, nickname, profileImage);
    }


    // 파티원 제외 (강퇴)
    @Transactional
    public PartyMemberRemoveResponse removeMember(Long partyId, Long partyMemberId, Long currentUserId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PARTY_NOT_FOUND));

        if (!party.getLeaderId().equals(currentUserId)) {
            throw new CustomException(CustomErrorCode.NOT_PARTY_LEADER);
        }

        PartyMember member = partyMemberRepository.findById(partyMemberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PARTY_MEMBER_NOT_FOUND));

        if (!member.getParty().getId().equals(partyId)) {
            throw new CustomException(CustomErrorCode.PARTY_MEMBER_NOT_MATCH);
        }

        if (member.getUser().getId().equals(party.getLeaderId())) {
            throw new CustomException(CustomErrorCode.CANNOT_KICK_LEADER);
        }

        // 1. 멤버 강퇴 (상태 LEFT)
        member.leaveParty();

        // 2. 상태 변경 (ACTIVE -> RECRUIT)
        // 만약 '게임 시작(ACTIVE)' 상태였는데 한 명이 나가면 -> 다시 '모집 중(RECRUIT)'으로 강등
        if (party.getStatus() == PartyStatus.ACTIVE) {
            party.downgradeToRecruit();
        }

        return PartyMemberRemoveResponse.from(member);
    }


    // 파티원 목록 조회
    public PartyMemberListResponse getPartyMemberList(Long partyId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PARTY_NOT_FOUND));

        Post post = postRepository.findById(party.getPostId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.POST_NOT_FOUND));

        List<PartyMember> members = partyMemberRepository.findActiveMembersByPartyId(partyId);

        List<PartyMemberListResponse.PartyMemberDto> memberDtos = new ArrayList<>();

        for (PartyMember member : members) {
            User user = member.getUser();
            String profileImage = (user.getProfileImage() != null)
                    ? user.getProfileImage()
                    : "https://opgg-static.akamaized.net/meta/images/profile_icons/profileIcon1.jpg";

            PartyMemberListResponse.PartyMemberDto dto = PartyMemberListResponse.PartyMemberDto.of(
                    member.getId(),
                    user.getId(),
                    member.getRole(),
                    member.getJoinedAt(),
                    user.getNickname(),
                    profileImage
            );
            memberDtos.add(dto);
        }

        return new PartyMemberListResponse(
                party.getId(),
                members.size(),
                post.getRecruitCount(),
                memberDtos
        );
    }
    // 내가 참여한 파티 목록 조회
    public MyPartyListResponse getMyPartyList(Long currentUserId) {
        List<PartyMember> myMemberships = partyMemberRepository.findAllByUserIdWithParty(currentUserId);

        if (myMemberships.isEmpty()) {
            return new MyPartyListResponse(List.of());
        }

        // 1. Post 정보 조회 (더 이상 GameMode를 Fetch Join할 필요 없음)
        List<Long> postIds = myMemberships.stream()
                .map(pm -> pm.getParty().getPostId())
                .toList();

        Map<Long, Post> postMap = postRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(Post::getId, post -> post));

        List<MyPartyListResponse.MyPartyDto> partyDtos = myMemberships.stream()
                .map(pm -> {
                    Party party = pm.getParty();
                    Post post = postMap.get(party.getPostId());

                    String postTitle = (post != null) ? post.getMemo() : "삭제된 게시글입니다.";

                    // [변경] Enum에서 바로 한글 이름 가져오기
                    String gameModeName = (post != null && post.getGameMode() != null)
                            ? post.getGameMode().getDescription() // "소환사의 협곡"
                            : "Unknown";



                    String queueType = (post != null && post.getQueueType() != null)
                            ? post.getQueueType().name() : null;

                    return MyPartyListResponse.MyPartyDto.of(
                            party.getId(),
                            party.getPostId(),
                            postTitle,
                            gameModeName, // "소환사의 협곡"
                            queueType,
                            party.getStatus(),
                            pm.getRole(),
                            pm.getJoinedAt()
                    );
                })
                .toList();

        return new MyPartyListResponse(partyDtos);
    }

    // 파티 상태 수동 종료
    @Transactional
    public PartyCloseResponse closeParty(Long partyId, Long currentUserId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PARTY_NOT_FOUND));

        if (!party.getLeaderId().equals(currentUserId)) {
            throw new CustomException(CustomErrorCode.NOT_PARTY_LEADER);
        }

        if (party.getStatus() == PartyStatus.CLOSED) {
            throw new CustomException(CustomErrorCode.PARTY_ALREADY_CLOSED);
        }

        // 상태 변경 (RECRUIT or ACTIVE -> CLOSED)
        party.closeParty();

        Post post = postRepository.findById(party.getPostId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.POST_NOT_FOUND));

        post.updateStatus(PostStatus.CLOSED);

        return new PartyCloseResponse(
                party.getId(),
                party.getStatus().name(),
                party.getClosedAt()
        );
    }
}