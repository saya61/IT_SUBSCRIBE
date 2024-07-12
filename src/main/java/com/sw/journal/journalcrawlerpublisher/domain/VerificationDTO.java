package com.sw.journal.journalcrawlerpublisher.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter @Setter
@RequiredArgsConstructor
public class VerificationDTO { // DTO 클래스 역할을 하는 클래스
    private String email;
    private String code;
}
