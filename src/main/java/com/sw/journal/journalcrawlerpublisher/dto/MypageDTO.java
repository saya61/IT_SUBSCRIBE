package com.sw.journal.journalcrawlerpublisher.dto;

import com.sw.journal.journalcrawlerpublisher.domain.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
// 마이페이지에 필요한 데이터를 전송하기 위한 DTO
// MemberDTO 에서 비밀번호만 제외한 DTO
public class MypageDTO {
    private String username; // 사용자 id
    private String nickname; // 사용자 닉네임
    private String email; // 사용자 이메일
    private String profileImageUrl; // 사용자 프로필 이미지 URL
    private LocalDateTime createdAt; // 사용자 생성 날짜

    // Member 객체를 받아 MypageDTO 필드를 초기화하는 생성자
    public MypageDTO(Member member) {
        this.username = member.getUsername(); // Member 객체에서 사용자 id를 가져와 초기화
        this.nickname = member.getNickname(); // Member 객체에서 사용자 닉네임을 가져와 초기화
        this.email = member.getEmail(); // Member 객체에서 사용자 이메일을 가져와 초기화
        // 사용자 프로필 이미지가 존재할 경우
        if (member.getProfileImage() != null) {
            // TODO : 고도화 시 저장소 유형 및 위치에 따라서
            //        DB 컬럼 추가 후 맞춤 URL 생성해 응답
            // TODO : 클라이언트가 원하는 동작에 맞게 URL 컨트롤이 가능해야 함
            this.profileImageUrl = member.getProfileImage().getFileUrl(); // Member 객체에서 프로필 이미지 URL 를 가져와 초기화
        } else { // 프로필 이미지가 없을 경우 null 로 초기화
            this.profileImageUrl = null;
        }
        this.createdAt = member.getCreatedAt(); // Member 객체에서 사용자 생성 날짜를 가져와 초기화
    }
}