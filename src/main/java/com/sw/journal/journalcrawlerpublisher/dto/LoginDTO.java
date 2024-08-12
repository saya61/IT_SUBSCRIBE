package com.sw.journal.journalcrawlerpublisher.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
// 로그인에 필요한 데이터를 전송하기 위한 DTO
public class LoginDTO {
    String id; // 사용자 아이디
    String password; // 사용자 비밀번호
}
