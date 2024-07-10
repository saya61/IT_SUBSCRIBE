package com.sw.journal.journalcrawlerpublisher.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter @Setter
@RequiredArgsConstructor
public class VerificationRequest { // DTO 클래스 역할을 하는 클래스
    private String username;
    private String nickname;
    private String email;
    private String code;
    private String password;

}
