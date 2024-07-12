package com.sw.journal.journalcrawlerpublisher.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ChangePwDTO {
    String newPw;
    String newPwConfirm;

    public ChangePwDTO(String newPw, String code, String email, String newPwConfirm) {
        this.newPw = newPw;
        this.newPwConfirm = newPwConfirm;
    }
}
