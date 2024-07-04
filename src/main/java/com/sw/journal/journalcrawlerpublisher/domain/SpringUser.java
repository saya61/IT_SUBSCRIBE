package com.sw.journal.journalcrawlerpublisher.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

// 스프링에서 인증을 수행하기 위한 User 클래스 (Wrapper로 작용)
public class SpringUser extends User{

    public SpringUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    public SpringUser(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    }

    // 엔티티 유저와 스프링 시큐리티 유저 간 데이터 주고받는 부분
    public static UserDetails getSpringUserDetails(Member appUser) {
        return User.builder().username(appUser.getUsername()).password(appUser.getPassword()).roles(appUser.getRole().toString()).build();
    }
}
