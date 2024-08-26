package com.sw.journal.journalcrawlerpublisher.config;

import org.springframework.kafka.support.serializer.JsonSerializer;
import com.sw.journal.journalcrawlerpublisher.dto.CrawlingEventDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    // Kafka 서버 주소
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    // Kafka 프로듀서를 위한 팩토리 메서드
    public ProducerFactory<String, CrawlingEventDTO> producerFactory() {
        Map<String, Object> configProps = new HashMap<>(); // Kafka 프로듀서 설정을 담을 Map
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress); // Kafka 클러스터 연결을 위한 서버 주소
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // 메시지 키의 직렬화 방식으로 StringSerializer 사용
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class); // 메시지 값의 직렬화 방식으로 JsonSerializer 사용
        return new DefaultKafkaProducerFactory<>(configProps); // 설정된 값을 바탕으로 Kafka 프로듀서 팩토리 생성 후 반환
    }

    @Bean
    // Kafka 메시지를 보내기 위한 KafkaTemplate 생성
    public KafkaTemplate<String, CrawlingEventDTO> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory()); // producerFactory()를 통해 KafkaTemplate 생성
    }
}

