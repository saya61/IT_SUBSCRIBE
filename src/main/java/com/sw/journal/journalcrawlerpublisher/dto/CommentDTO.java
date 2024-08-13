package com.sw.journal.journalcrawlerpublisher.dto;

import com.sw.journal.journalcrawlerpublisher.domain.ProfileImage;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Profile;

@Data
@Getter
@Setter
// 댓글 데이터를 전송하기 위한 DTO
public class CommentDTO {
    private Long id; // 댓글 id
    private String content; // 댓글 내용
    private Long articleId; // 댓글을 작성한 기사의 id
    private Long memberId; // 댓글 작성자의 id
    private String memberNickname; // 댓글 작성자의 닉네임
    private String profileImageURL; // 댓글 작성자의 프로필 이미지 URL
}
