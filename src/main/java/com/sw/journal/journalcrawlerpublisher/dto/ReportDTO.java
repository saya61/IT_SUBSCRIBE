package com.sw.journal.journalcrawlerpublisher.dto;

import com.sw.journal.journalcrawlerpublisher.constant.CommentReportStatus;
import com.sw.journal.journalcrawlerpublisher.constant.ReportReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public class ReportDTO {

    // Request DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private Long commentId;
        private Long reporterId;
        private ReportReason reason;
    }

    // Update DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {
        private Long reportId;
        private CommentReportStatus status;
    }

    // Response DTO
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        // Comment 정보
        private Long commentId;              // 댓글 ID
        private String commentContent;       // 댓글 내용
        private Long articleId;              // 기사 ID

        // 작성자 정보
        private UserInfo commentator;        // 작성자 정보

        // 신고자 정보
        private UserInfo reporter;           // 신고자 정보

        // Report 정보
        private Long reportId;               // 신고 ID
        private LocalDateTime reportDate;    // 신고 날짜
        private String reason;               // 신고 사유
        private String status;               // 신고 상태
    }

    // member 의 ID만 반환하려다가 username 도 같이 반환하도록 했습니다.
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private Long id;                    // 유저 ID
        private String userName;            // 유저 이름
    }
}
