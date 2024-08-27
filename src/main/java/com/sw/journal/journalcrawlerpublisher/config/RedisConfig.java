package com.sw.journal.journalcrawlerpublisher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(host, port)
        );
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory0() {
        RedisStandaloneConfiguration redisConf = new RedisStandaloneConfiguration(
                host, port
        );
        redisConf.setDatabase(0);
        return new LettuceConnectionFactory(redisConf);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(
            RedisConnectionFactory redisConnectionFactory
    ) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    @Bean
    public StringRedisTemplate redisTemplateDb0() {
        return new StringRedisTemplate(redisConnectionFactory0());
    }

    @Bean
    public RedisTemplate<String, String> redisObjTemplateDb0() {  // Redis 키는 String 값은 Object
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory0());

        // Long 타입 값을 직렬화하기 위한 직렬화기
        GenericToStringSerializer<Long> longSerializer = new GenericToStringSerializer<>(Long.class);

        // 객체 타입 값을 직렬화하기 위한 직렬화기
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        // 조건에 따라 serializer 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}
