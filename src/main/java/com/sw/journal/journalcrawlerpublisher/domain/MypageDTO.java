package com.sw.journal.journalcrawlerpublisher.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
// MemberDTO 에서 비밀번호만 제외한 DTO
public class MypageDTO {
    private String id;
    private String nickname;
    private String email;
    private String profileImageUrl;
    private LocalDateTime createdAt;

    public MypageDTO(Member member) {
        this.id = String.valueOf(member.getId());
        this.nickname = member.getNickname();
        this.email = member.getEmail();
        // 유저 프로필 이미지 URL
        if (member.getProfileImage() != null) {
            // TODO : 고도화 시 저장소 유형 및 위치에 따라서
            //        DB컬럼 추가 후 맞춤 URL 생성해 응답
            // TODO : 클라이언트가 원하는 동작에 맞게 URL 컨트롤이 가능해야 함
            //        현재 : 파일로 다운로드 할 수 있는 엔드포인트로 연결
            //
            this.profileImageUrl = "/api/members/mypage/get-profile-image?filename=" + member.getProfileImage().getFileName();
        } else {
            this.profileImageUrl = null;
        }
        this.createdAt = member.getCreatedAt();
    }
}
