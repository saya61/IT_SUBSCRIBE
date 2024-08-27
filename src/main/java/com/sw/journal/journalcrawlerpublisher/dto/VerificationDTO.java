package com.sw.journal.journalcrawlerpublisher.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
// Kafka 로 인증 번호에 대한 메시지를 전송하기 위한 DTO 클래스
public class VerificationDTO {
    private String email; // 사용자 이메일
    private String code; // 인증 번호
    private LocalDateTime createdAt; // 생성 날짜

    public VerificationDTO(String email, String code, LocalDateTime createdAt) {
        this.email = email;
        this.code = code;
        this.createdAt = createdAt;
    }
}
