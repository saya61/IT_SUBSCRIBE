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
    private String profileImageUrl;

    public MypageDTO(Member member) {
        this.username = member.getUsername();
        this.nickname = member.getNickname();
        this.email = member.getEmail();
        // 유저 프로필 이미지 URL
        if (member.getProfileImage() != null) {
            this.profileImageUrl = "/api/members/mypage/get-profile-image?filename=" + member.getProfileImage().getFileName();
        } else {
            this.profileImageUrl = null;
        }
    }
}
