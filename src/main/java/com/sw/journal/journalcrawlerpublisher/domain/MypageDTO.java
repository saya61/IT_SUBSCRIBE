package com.sw.journal.journalcrawlerpublisher.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
// MemberDTO에서 비밀번호만 제외한 DTO
public class MypageDTO {
    private String username;
    private String nickname;
    private String email;
    private ProfileImage profileImageId;

    public MypageDTO(Member member) {
        this.username = member.getUsername();
        this.nickname = member.getNickname();
        this.email = member.getEmail();
        this.profileImageId = member.getProfileImage();
    }
}
