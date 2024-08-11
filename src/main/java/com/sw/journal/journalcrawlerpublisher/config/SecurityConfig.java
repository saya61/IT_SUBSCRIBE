package com.sw.journal.journalcrawlerpublisher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 비활성화 (개발 시 또는 특정 조건에서만 사용, 기본적으로는 활성화 필요)
                .csrf(csrf -> csrf.disable())
                // CORS 설정 추가
                //.cors(withDefaults())
                // 요청에 대한 인가(Authorization) 설정
                .authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests
                                // "/api/members/mypage/**" 경로는 인증 없이 접근 허용
                                .requestMatchers("/api/members/mypage/**").permitAll()
                                // 로그인 및 회원가입 페이지는 인증 없이 접근 허용
                                .requestMatchers("/members/login", "/members/register").permitAll()
                                // "/recommend-article/**" 경로는 인증이 필요
                                .requestMatchers("/recommend-article/**").authenticated()
                                // 그 외의 모든 요청은 인증 없이 접근 허용
                                .anyRequest().permitAll()
                )
                // 로그인 설정
                .formLogin(formLogin ->
                        formLogin
                                // 로그인 페이지의 경로 설정
                                .loginPage("/members/login")
                                // 로그인 성공 시 리다이렉트할 기본 경로 설정
                                .defaultSuccessUrl("/")
                )
                // 세션 관리 설정
                .sessionManagement(sessionManagement ->
                        sessionManagement
                                // 세션을 필요할 때만 생성하도록 설정
                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                // 로그인 시 세션 고정 공격 방지를 위해 세션 ID 변경
                                .sessionFixation().migrateSession()
                )
                // 로그아웃 설정
                .logout(logout ->
                        logout
                                // 로그아웃 요청을 처리할 경로 설정
                                .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                                // 로그아웃 성공 시 리다이렉트할 경로 설정
                                .logoutSuccessUrl("/")
                                // 로그아웃 시 세션 무효화
                                .invalidateHttpSession(true)
                );
        // Security 설정을 적용한 SecurityFilterChain 객체를 생성 및 반환
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder를 사용하여 비밀번호를 암호화
        return new BCryptPasswordEncoder();
    }
}
