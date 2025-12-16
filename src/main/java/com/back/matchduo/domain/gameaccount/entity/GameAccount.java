package com.back.matchduo.domain.gameaccount.entity;

import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "game_account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_account_id")
    private Long gameAccountId;

    @Column(name = "game_nickname", nullable = false)
    private String gameNickname;

    @Column(name = "game_tag")
    private String gameTag;

    @Column(name = "game_type")
    private String gameType;

    @Column(name = "puuid")
    private String puuid;

    @Column(name = "profile_icon_id")
    private Integer profileIconId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Builder
    public GameAccount(String gameNickname, String gameTag, String gameType, String puuid, Integer profileIconId, User user) {
        this.gameNickname = gameNickname;
        this.gameTag = gameTag;
        this.gameType = gameType;
        this.puuid = puuid;
        this.profileIconId = profileIconId;
        this.user = user;
    }

    /**
     * @param gameNickname 새로운 닉네임
     * @param gameTag 새로운 태그
     * @param puuid 새로운 puuid
     */
    public void update(String gameNickname, String gameTag, String puuid) {
        this.gameNickname = gameNickname;
        this.gameTag = gameTag;
        if (puuid != null) {
            this.puuid = puuid;
        }
        // createdAt은 @CreatedDate와 updatable = false로 보호됨
        // updatedAt은 @LastModifiedDate로 자동 업데이트됨
    }

    /**
     * 프로필 아이콘 ID 업데이트
     * @param profileIconId 새로운 프로필 아이콘 ID
     */
    public void updateProfileIconId(Integer profileIconId) {
        this.profileIconId = profileIconId;
    }
}
