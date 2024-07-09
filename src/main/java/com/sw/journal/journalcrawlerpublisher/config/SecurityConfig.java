package com.sw.journal.journalcrawlerpublisher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(
                (authorizeHttpRequests) ->
                    authorizeHttpRequests
                        .requestMatchers(
                            new AntPathRequestMatcher("/api/**", HttpMethod.POST.name())
                        ).permitAll()
            )
            .formLogin(
                    (formLogin) ->
                            formLogin
                                    .loginPage("/members/login")
                                    .defaultSuccessUrl("/")
            )
            .logout(
                    (logout) ->
                            logout
                                    .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                                    .logoutSuccessUrl("/")
                                    .invalidateHttpSession(true)
            )
        ;
        return http.build();
    }

    // passwordEncoder 빈 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
