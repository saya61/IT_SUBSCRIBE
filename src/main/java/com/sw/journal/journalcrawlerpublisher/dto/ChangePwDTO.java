package com.sw.journal.journalcrawlerpublisher.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
// 비밀번호 재설정에 필요한 데이터를 전송하기 위한 DTO
public class ChangePwDTO {
    String newPw; // 새 비밀번호
    String newPwConfirm; // 새 비밀번호 확인

    // 매개변수로 필드를 받아 초기화하는 생성자
    public ChangePwDTO(String newPw, String code, String email, String newPwConfirm) {
        this.newPw = newPw; // 새 비밀번호 필드 초기화
        this.newPwConfirm = newPwConfirm; // 새 비밀번호 확인 필드 초기화
    }
}
