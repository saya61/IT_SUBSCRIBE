package com.sw.journal.journalcrawlerpublisher.dto;

import com.sw.journal.journalcrawlerpublisher.domain.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class MemberDTO {
    private String username;
    private String nickname;
    private String email;
    private String password;
    private String password2;

    public MemberDTO(Member member) {
        this.username = member.getUsername();
        this.nickname = member.getNickname();
        this.email = member.getEmail();
        this.password = member.getPassword();
        this.password2 = member.getPassword();
    }
}
