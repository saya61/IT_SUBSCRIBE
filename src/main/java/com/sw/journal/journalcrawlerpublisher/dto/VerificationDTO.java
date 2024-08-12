package com.sw.journal.journalcrawlerpublisher.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter @Setter
@RequiredArgsConstructor
// 인증번호 검증에 필요한 데이터를 전송하기 위한 DTO
public class VerificationDTO {
    private String email; // 사용자 이메일
    private String code; // 인증번호
}
