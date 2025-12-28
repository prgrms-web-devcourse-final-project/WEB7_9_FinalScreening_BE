package com.back.matchduo.domain.user.entity;

import com.back.matchduo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 40, nullable = false, unique = true)
    private String email;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(length = 20, nullable = false, unique = true)
    private String nickname;

    @Column(length = 100)
    private String comment;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "verification_code", length = 100, nullable = true)
    private String verificationCode;

    private LocalDateTime nicknameUpdatedAt;

    public static User createUser(String email, String password, String nickname) {
        return User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .verificationCode("VERIFIED") // DB가 NULL을 거부하므로 명시적으로 값을 넣어줌
                .build();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setNicknameUpdatedAt(LocalDateTime nicknameUpdatedAt) {this.nicknameUpdatedAt = nicknameUpdatedAt;}
}
