package com.sw.journal.journalcrawlerpublisher.constant;

import lombok.Getter;

@Getter
public enum CommentReportStatus {
    PENDING("대기중"),
    IN_PROGRESS("처리중"),
    REJECTED("반려"),
    APPROVED("승인");


    // 설명을 반환하는 메서드
    private final String description;

    // 생성자
    CommentReportStatus(String description) {
        this.description = description;
    }

}
