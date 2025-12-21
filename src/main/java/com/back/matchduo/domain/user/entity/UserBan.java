package com.back.matchduo.domain.user.entity;

import com.back.matchduo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_bans",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"from_user_id", "to_user_id"})
        })
public class UserBan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser; // 차단한 유저

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser; // 차단당한 유저

    public static UserBan createBan(User fromUser, User toUser) {
        return UserBan.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .build();
    }
}