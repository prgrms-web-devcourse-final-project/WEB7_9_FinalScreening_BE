package com.back.matchduo.domain.party.service;

import com.back.matchduo.domain.party.dto.request.PartyMemberAddRequest;
import com.back.matchduo.domain.party.dto.response.*;
import com.back.matchduo.domain.party.entity.*;
import com.back.matchduo.domain.party.repository.PartyMemberRepository;
import com.back.matchduo.domain.party.repository.PartyRepository;
import com.back.matchduo.domain.post.entity.Post;
import com.back.matchduo.domain.post.repository.PostRepository;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.repository.UserRepository;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

            // [수정] 실제 User 객체에서 데이터 추출
            User user = member.getUser();
            String nickname = user.getNickname();
            // User 엔티티에 profileImage 필드가 없다면 임시값 유지하거나 추가 필요
            String profileImage = "https://opgg-static.akamaized.net/meta/images/profile_icons/profileIcon1.jpg";

            PartyByPostResponse.PartyMemberDto dto = PartyByPostResponse.PartyMemberDto.of(
                    member.getId(),
                    user.getId(),
                    nickname,
                    profileImage,
                    member.getRole()
            );
            memberDtos.add(dto);
        }

        // 5. 모집글 정보 조회 (Max Count)
        Post post = postRepository.findById(party.getPostId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.POST_NOT_FOUND)); // POST_NOT_FOUND 에러코드 필요

        int maxCount = post.getRecruitCount(); // Post 엔티티 필드 사용

        // 6. 응답 DTO 반환
        return new PartyByPostResponse(
                party.getId(),
                party.getPostId(),
                party.getStatus(),
                memberDtos.size(),
                maxCount,
                party.getCreatedAt(),
                isJoined,
                memberDtos
        );
    }


    // 파티원 추가
    @Transactional
    public List<PartyMemberAddResponse> addMembers(Long partyId, Long currentUserId, PartyMemberAddRequest request) {
        // 1. 파티 및 리더 조회
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PARTY_NOT_FOUND));

        // 2. [검증] 요청자가 '파티장'인지 확인
        if (!party.getLeaderId().equals(currentUserId)) {
            throw new CustomException(CustomErrorCode.NOT_PARTY_LEADER);
        }

        // 3. [검증] 파티 상태 확인
        if (party.getStatus() == PartyStatus.CLOSED) {
            throw new CustomException(CustomErrorCode.PARTY_ALREADY_CLOSED);
        }

        List<PartyMemberAddResponse> responses = new ArrayList<>();

        for (Long targetUserId : request.targetUserIds()) {
            // 4. [검증] 이미 참여 중인지 확인
            Optional<PartyMember> existingMemberOpt = partyMemberRepository.findByPartyIdAndUserId(partyId, targetUserId);

            if (existingMemberOpt.isPresent()) {
                PartyMember existingMember = existingMemberOpt.get();
                if (existingMember.getState() == PartyMemberState.JOINED) {
                    throw new CustomException(CustomErrorCode.PARTY_ALREADY_JOINED);
                }
                // 재가입 (LEFT -> JOINED)
                existingMember.rejoinParty();
                responses.add(createAddResponse(existingMember));
            } else {
                // 신규 가입 시 User 엔티티 조회 필요
                User targetUser = userRepository.findById(targetUserId)
                        .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));

                // 생성자에 User 객체 전달
                PartyMember newMember = new PartyMember(party, targetUser, PartyMemberRole.MEMBER);
                partyMemberRepository.save(newMember);
                responses.add(createAddResponse(newMember));
            }
        }

        return responses;
    }

    // DTO 변환 편의 메서드
    private PartyMemberAddResponse createAddResponse(PartyMember member) {
        // User 객체에서 정보 추출
        User user = member.getUser();
        String nickname = user.getNickname();
        String profileImage = "https://dummy.img/" + user.getId(); // 임시

        //  DTO 팩토리 메서드 호출 (User ID 전달)
        return PartyMemberAddResponse.of(member, nickname, profileImage);
    }


    // 파티원 제외 (강퇴)
    @Transactional
    public PartyMemberRemoveResponse removeMember(Long partyId, Long partyMemberId, Long currentUserId) {
        // 1. 파티 조회
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PARTY_NOT_FOUND));

        // 2. [권한 검증] 요청자가 파티장인지 확인
        if (!party.getLeaderId().equals(currentUserId)) {
            throw new CustomException(CustomErrorCode.NOT_PARTY_LEADER);
        }

        // 3. 대상 멤버 조회
        PartyMember member = partyMemberRepository.findById(partyMemberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PARTY_MEMBER_NOT_FOUND));

        // 4. [검증] 해당 멤버가 진짜 이 파티 소속인지 확인
        if (!member.getParty().getId().equals(partyId)) {
            throw new CustomException(CustomErrorCode.PARTY_MEMBER_NOT_MATCH);
        }

        // 5. [검증] 파티장이 자기 자신을 강퇴하려는 경우 방지
        if (member.getUser().getId().equals(party.getLeaderId())) {
            throw new CustomException(CustomErrorCode.CANNOT_KICK_LEADER);
        }

        // 6. 상태 변경
        member.leaveParty();

        // 7. 응답 반환
        return PartyMemberRemoveResponse.from(member);
    }


     // 파티원 목록 조회 (로그인 불필요)
    @Transactional(readOnly = true)
    public PartyMemberListResponse getPartyMemberList(Long partyId) {
        // 1. 파티 정보 조회
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PARTY_NOT_FOUND));

        // 2. [수정] 모집글(Post) 조회 (MaxCount 확인용)
        Post post = postRepository.findById(party.getPostId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.POST_NOT_FOUND));

        // 3. 참여 중인 멤버 목록 조회 (Join Fetch 사용)
        List<PartyMember> members = partyMemberRepository.findActiveMembersByPartyId(partyId);

        // 4. DTO 변환
        List<PartyMemberListResponse.PartyMemberDto> memberDtos = members.stream()
                .map(member -> {
                    User user = member.getUser();
                    String profileImage = "https://opgg-static.akamaized.net/meta/images/profile_icons/profileIcon" + user.getId() + ".jpg";

                    return PartyMemberListResponse.PartyMemberDto.of(
                            member.getId(),
                            user.getId(),
                            member.getRole(),
                            member.getJoinedAt(),
                            user.getNickname(),
                            profileImage
                    );
                })
                .toList();

        // 5. 응답 반환
        return new PartyMemberListResponse(
                party.getId(),
                members.size(),
                post.getRecruitCount(),
                memberDtos
        );
    }



    // 내가 참여한 파티 목록 조회
    @Transactional(readOnly = true)
    public MyPartyListResponse getMyPartyList(Long currentUserId) {
        // 1. 내가 참여한 파티 멤버십 조회 (Party 정보 포함)
        List<PartyMember> myMemberships = partyMemberRepository.findAllByUserIdWithParty(currentUserId);

        if (myMemberships.isEmpty()) {
            return new MyPartyListResponse(List.of());
        }

        // 2. 게시글(Post) ID 목록 추출
        List<Long> postIds = myMemberships.stream()
                .map(pm -> pm.getParty().getPostId())
                .toList();

        // 3. 게시글 정보 일괄 조회 (GameMode 포함) -> Map으로 변환 (조회 성능 최적화)
        Map<Long, Post> postMap = postRepository.findAllByIdInWithGameMode(postIds).stream()
                .collect(Collectors.toMap(Post::getId, post -> post));

        // 4. DTO 변환
        List<MyPartyListResponse.MyPartyDto> partyDtos = myMemberships.stream()
                .map(pm -> {
                    Party party = pm.getParty();
                    Post post = postMap.get(party.getPostId());

                    // Post가 삭제되었을 경우에 대한 안전장치 (Optional)
                    String postTitle = (post != null) ? post.getMemo() : "삭제된 게시글입니다.";

                    String gameModeName = (post != null) ? post.getGameMode().getName() : "Unknown";

                    return MyPartyListResponse.MyPartyDto.of(
                            party.getId(),
                            party.getPostId(),
                            postTitle,
                            gameModeName,
                            party.getStatus(),
                            pm.getRole(),
                            pm.getJoinedAt()
                    );
                })
                .toList();

        return new MyPartyListResponse(partyDtos);
    }
}