package com.back.matchduo.global.security;

import com.back.matchduo.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor; // ★ 이 어노테이션이 생성자를 만들어줍니다.
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    // 서비스/컨트롤러에서 사용할 ID 반환 메서드
    public Long getId() {
        return user.getId();
    }

    // --- UserDetails 필수 메서드 구현 ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 임시로 USER 권한 부여
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // 시큐리티 username = 우리 서비스 email
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}