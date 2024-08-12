package com.sw.journal.journalcrawlerpublisher.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter @Setter
@RequiredArgsConstructor
public class DuplicationCheckDTO { // DTO 클래스 역할을 하는 클래스
    private String username;
    private String nickname;
    private String email;
}