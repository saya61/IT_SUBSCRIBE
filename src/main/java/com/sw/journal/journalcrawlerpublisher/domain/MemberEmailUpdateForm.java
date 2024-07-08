package com.sw.journal.journalcrawlerpublisher.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberEmailUpdateForm { // DTO 클래스 역할을 하는 Form 클래스
    @NotEmpty(message = "변경할 이메일 주소을 입력해주세요.")
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    private String email;
}
