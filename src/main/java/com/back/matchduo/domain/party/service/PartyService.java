package com.back.matchduo.domain.party.service;

import com.back.matchduo.domain.party.dto.response.PartyByPostResponse;
import com.back.matchduo.domain.party.entity.Party;
import com.back.matchduo.domain.party.entity.PartyMember;
import com.back.matchduo.domain.party.entity.PartyMemberState;
import com.back.matchduo.domain.party.repository.PartyMemberRepository;
import com.back.matchduo.domain.party.repository.PartyRepository;
import com.back.matchduo.domain.post.repository.PostRepository;
import com.back.matchduo.domain.user.repository.UserRepository;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyService {

    private final PartyRepository partyRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public PartyByPostResponse getPartyByPostId(Long postId, Long currentUserId) {
        // 1. 파티 정보 조회 (없으면 예외 발생)
        Party party = partyRepository.findByPostId(postId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PARTY_NOT_FOUND));

        // 2. 파티에 속한 모든 멤버 조회
        List<PartyMember> allMembers = partyMemberRepository.findByPartyId(party.getId());

        // 3. 현재 유저의 참여 여부 확인
        boolean isJoined = false;

        if (currentUserId != null) {
            for (PartyMember member : allMembers) {
                if (member.getUserId().equals(currentUserId) && member.getState() == PartyMemberState.JOINED) {
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

            // UserService 구현 전 임시, 실제로는 userId로 유저 닉네임, 프로필 이미지 조회
            String nickname = "소환사" + member.getUserId();
            String profileImage = "https://opgg-static.akamaized.net/meta/images/profile_icons/profileIcon1.jpg";

            PartyByPostResponse.PartyMemberDto dto = PartyByPostResponse.PartyMemberDto.of(
                    member.getId(),
                    member.getUserId(),
                    nickname,
                    profileImage,
                    member.getRole()
            );
            memberDtos.add(dto);
        }

        // 5. 모집글 정보 조회 (임시 값)
        // Post Service 구현될 시 대체
        int maxCount = 5;

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
}
