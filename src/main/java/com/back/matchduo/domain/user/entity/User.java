package com.back.matchduo.domain.user.entity;

import com.back.matchduo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
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

    @Column
    private String profile_image;

    @Column(length = 100, nullable = false)
    private String verification_code;

    public static User createUser(String email, String password, String nickname) {
        User user = new User();
        user.email = email;
        user.password = password;
        user.nickname = nickname;
        return user;
    }
}
