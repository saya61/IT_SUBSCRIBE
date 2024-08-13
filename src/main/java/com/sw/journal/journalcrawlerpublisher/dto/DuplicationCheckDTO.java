package com.sw.journal.journalcrawlerpublisher.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter @Setter
@RequiredArgsConstructor
// 아이디, 닉네임, 이메일 중복 확인에 필요한 데이터를 전송하기 위한 DTO
public class DuplicationCheckDTO {
    private String username; // 사용자 아이디
    private String nickname; // 사용자 닉네임
    private String email; // 사용자 이메일
}