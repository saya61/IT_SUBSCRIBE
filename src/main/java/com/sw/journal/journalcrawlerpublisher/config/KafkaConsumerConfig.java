package com.sw.journal.journalcrawlerpublisher.config;

import com.sw.journal.journalcrawlerpublisher.dto.CrawlingEventDTO;
import com.sw.journal.journalcrawlerpublisher.dto.VerificationDTO;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka // Kafka 리스너 활성화 -> Kafka 메시지를 소비할 수 있게 만듦
@Configuration
public class KafkaConsumerConfig {
    // Kafka 서버 주소
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;
    // Kafka 컨슈머 그룹 ID
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    // ====== 사용자 선호 카테고리 알람 기능을 위한 컨슈머 설정 ======
    // Kafka 컨슈머를 위한 팩토리 메서드
    public ConsumerFactory<String, CrawlingEventDTO> consumerFactory() {
        Map<String, Object> props = new HashMap<>(); // Kafka 컨슈머 설정을 담을 Map
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress); // Kafka 클러스터 연결을 위한 서버 주소
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId); // 컨슈머가 속할 그룹 ID
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // 메시지 키의 역직렬화 방식으로 StringSerializer 사용
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class); // 메시지 값의 역직렬화 방식으로 JsonSerializer 사용
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.sw.journal.journalcrawlerpublisher.dto"); //  JSON 역직렬화 시 신뢰할 수 있는 패키지 지정
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CrawlingEventDTO.class.getName()); // 기본으로 역직렬화할 DTO 타입 지정 (JSON 메시지를 CrawlingEventDTO 로 변환)
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // 자동 커밋 비활성화 -> 수동 커밋으로 메시지 처리
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
                new JsonDeserializer<>(CrawlingEventDTO.class)); // 설정된 값을 바탕으로 Kafka 컨슈머 팩토리 생성 후 반환
    }

    @Bean
    // Kafka 메시지를 소비할 KafkaListenerContainer 생성 및 설정
    public ConcurrentKafkaListenerContainerFactory<String, CrawlingEventDTO> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CrawlingEventDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>(); // KafkaListener를 위한 ConcurrentKafkaListenerContainerFactory 생성
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL); // 메시지 커밋 수동 처리를 위한 AckMode 설정
        factory.setConsumerFactory(consumerFactory()); // consumerFactory()를 통해 생성한 ConsumerFactory 설정
        return factory;
    }

    @Bean
    // ====== 이메일 인증 번호 기능을 위한 컨슈머 설정 ======
    // Kafka 컨슈머를 위한 팩토리 메서드
    public ConsumerFactory<String, VerificationDTO> verificationConsumerFactory() {
        Map<String, Object> props = new HashMap<>(); // Kafka 컨슈머 설정을 담을 Map
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress); // Kafka 클러스터 연결을 위한 서버 주소
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId); // 컨슈머가 속할 그룹 ID
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // 메시지 키의 역직렬화 방식으로 StringSerializer 사용
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class); // 메시지 값의 역직렬화 방식으로 JsonSerializer 사용
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.sw.journal.journalcrawlerpublisher.dto"); //  JSON 역직렬화 시 신뢰할 수 있는 패키지 지정
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, VerificationDTO.class.getName()); // 기본으로 역직렬화할 DTO 타입 지정 (JSON 메시지를 VerificationDTO 로 변환)
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // 자동 커밋 비활성화 -> 수동 커밋으로 메시지 처리
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
                new JsonDeserializer<>(VerificationDTO.class)); // 설정된 값을 바탕으로 Kafka 컨슈머 팩토리 생성 후 반환
    }

    // Kafka 메시지를 소비할 KafkaListenerContainer 생성 및 설정
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VerificationDTO> verificationKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, VerificationDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>(); // KafkaListener를 위한 ConcurrentKafkaListenerContainerFactory 생성
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL); // 메시지 커밋 수동 처리를 위한 AckMode 설정
        factory.setConsumerFactory(verificationConsumerFactory()); // verificationConsumerFactory()를 통해 생성한 ConsumerFactory 설정
        return factory;
    }
}

