package com.sw.journal.journalcrawlerpublisher.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter @Setter
@RequiredArgsConstructor
public class VerificationResponse {  // DTO 클래스 역할을 하는 클래스
    private final String email;
    private final String code;
    private final String result;
}