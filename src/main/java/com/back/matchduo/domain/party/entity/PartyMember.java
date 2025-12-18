package com.back.matchduo.domain.party.entity;

import com.back.matchduo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        // 파티 중복 참여 방지 unique
        name = "party_member",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_party_member_user",
                        columnNames = {"party_id", "user_id"}
                )
        }
)
public class PartyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "party_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartyMemberRole role; // LEADER, MEMBER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartyMemberState state; // JOINED, LEFT

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;


    public PartyMember(Party party, User user, PartyMemberRole role) {
        this.party = party;
        this.user = user;
        this.role = role;
        this.state = PartyMemberState.JOINED;
        this.joinedAt = LocalDateTime.now();
    }


    public void leaveParty() {
        this.state = PartyMemberState.LEFT;
        this.leftAt = LocalDateTime.now();
    }

    // 나갔던 유저가 다시 들어올 때 사용
    public void rejoinParty() {
        this.state = PartyMemberState.JOINED;
        this.joinedAt = LocalDateTime.now();
        this.leftAt = null;
    }
}