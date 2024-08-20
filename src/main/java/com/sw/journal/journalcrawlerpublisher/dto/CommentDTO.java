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
    private Long id;                        // 댓글 id
    private String content;                 // 댓글 내용
    private Long articleId;                 // 댓글을 작성한 기사의 id
    private Long memberId;                  // 댓글 작성자의 id
    private String memberNickname;          // 댓글 작성자의 닉네임
    private String profileImageURL;         // 댓글 작성자의 프로필 이미지 URL
    private Long parentCommentId;           // 부모 댓글 ID (대댓글인 경우), 최상위 댓글인 경우 null
    private int replyCount;                 // 대댓글 수, for 댓글 더보기 버튼에 표시
    private int likeCount;                  // 좋아요 수
    private String relativeTime;            // 몇 분 전, 몇 시간 전 등 상대 시간
    private boolean isDeleted;              // 삭제 여부
    // private boolean isNotificationEnabled; // 사용자가 이 댓글에 대한 알림을 설정했는지 여부
}
