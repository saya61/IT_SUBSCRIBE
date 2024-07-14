package com.sw.journal.journalcrawlerpublisher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories // Spring Data Redis 리포지토리를 활성화
// 리포지토리 인터페이스 정의와 사용 가능
public class RedisConfig {
    // application.properties에서 가져옴
    @Value("${spring.data.redis.host}") // Redis 서버 호스트
    private String host;

    @Value("${spring.data.redis.port}") // Redis 서버 포트
    private int port;

    @Bean
    // RedisConnectionFactory : Redis 서버와의 연결을 생성하는 팩토리 인터페이스
    public RedisConnectionFactory redisConnectionFactory() {
        // return createConnectionFactoryWith(0);  // 0번 데이터베이스 사용
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    // RedisTemplate<String, Object> : Redis에 데이터를 저장하고 조회
    public RedisTemplate<String, String> redisTemplate() {  // Redis 키는 String 값은 Object
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());

        // Long 타입 값을 직렬화하기 위한 직렬화기
        GenericToStringSerializer<Long> longSerializer = new GenericToStringSerializer<>(Long.class);

        // 객체 타입 값을 직렬화하기 위한 직렬화기
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        // 조건에 따라 serializer 설정
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        return template;
    }
}
