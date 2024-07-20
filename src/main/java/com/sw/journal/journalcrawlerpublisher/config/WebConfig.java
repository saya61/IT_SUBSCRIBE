package com.sw.journal.journalcrawlerpublisher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration // 이 클래스가 하나 이상의 @Bean 메서드를 선언하고 스프링 컨테이너에서 Bean 정의를 생성하고 런타임 시 해당 Bean을 요청할 때 반환할 것을 나타냅니다.
public class WebConfig implements WebMvcConfigurer { // WebMvcConfigurer 인터페이스를 구현하는 WebConfig라는 이름의 설정 클래스를 정의

    @Bean // 이 메서드가 스프링 컨테이너에 의해 관리되는 Bean을 생성함을 나타냅니다.
    public WebMvcConfigurer corsConfigurer() { // corsConfigurer라는 이름의 Bean을 정의하는 메서드입니다.
        return new WebMvcConfigurer() { // WebMvcConfigurer의 익명 클래스를 반환
            @Override // 부모 인터페이스인 WebMvcConfigurer의 메서드를 오버라이드
            public void addCorsMappings(@Nullable CorsRegistry registry) { // addCorsMappings 메서드는 CORS 설정을 구성 registry 매개변수는 null을 허용할 수 있습니다.
                if (registry == null) {
                    return;
                }// registry가 null이 아닌 경우에만 실행됩니다.
                registry.addMapping("/**") // 모든 경로에 대한 CORS 설정을 추가
                        .allowedOrigins("http://localhost:3000") // http://localhost:3000에서 오는 요청을 허용
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD") // GET, POST, PUT, DELETE, HEAD 메서드를 허용
                        .allowedHeaders("*") // 모든 헤더를 허용
                        .allowCredentials(true); // 쿠키 및 인증 정보를 허용
            }
        };
    }
}