package com.sw.journal.journalcrawlerpublisher.domain;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberNicknameUpdateForm { // DTO 클래스 역할을 하는 Form 클래스
    @NotEmpty(message = "변경할 닉네임을 입력해주세요.")
    private String nickname;
}
