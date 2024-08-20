package com.sw.journal.journalcrawlerpublisher.dto;

import com.sw.journal.journalcrawlerpublisher.domain.ProfileImage;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Profile;


@Data
@Getter
@Setter
public class ReplyDTO {
    private Long id;                        // 대댓글 id
    private String content;                 // 대댓글 내용
    private Long memberId;                  // 대댓글 작성자의 id
    private String memberNickname;          //댓글 작성자의 닉네임
    private String profileImageURL;         // 댓글 작성자의 프로필 이미지 URL
    private int likeCount;                  // 좋아요 수
    private Long parentCommentId;           // 부모 댓글 ID
    private String relativeTime; // 몇 분 전, 몇 시간 전 등 상대 시간

}