package com.sw.journal.journalcrawlerpublisher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean
    // WebMvcConfigurer를 반환하는 메서드 정의. 이 메서드는 CORS 설정을 위한 빈을 생성함
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() { // 익명 클래스를 사용하여 WebMvcConfigurer 인터페이스를 구현
            @Override // WebMvcConfigurer의 메서드를 재정의
            public void addCorsMappings(CorsRegistry registry) { // CORS 매핑을 추가하기 위한 메서드
                registry.addMapping("/**") // 모든 경로에 대해 CORS 설정을 적용
                        .allowedOrigins("http://itscribe.site", "http://www.itscribe.site", "http://localhost:3000/") // React 앱의 URL
                        // 로컬 개발 환경에서 프론트엔드와 백엔드가 다른 포트에서 동작하는 경우에 사용됨
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 이 HTTP 메서드들을 허용
                        .allowedHeaders("*") // 모든 헤더를 허용
                        .allowCredentials(true); // 자격 증명(쿠키, 인증 헤더 등)을 포함한 요청을 허용
            }
        };
    }
}