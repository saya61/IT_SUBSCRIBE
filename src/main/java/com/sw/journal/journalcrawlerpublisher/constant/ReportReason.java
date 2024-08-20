package com.sw.journal.journalcrawlerpublisher.constant;

import lombok.Getter;

//유튜브 댓글신고 분류를 참고함

@Getter
public enum ReportReason {
    SEXUAL_CONTENT("성적인 콘텐츠"),
    VIOLENT_OR_HATEFUL_CONTENT("폭력적 또는 혐오스러운 콘텐츠"),
    HATEFUL_OR_MALICIOUS_CONTENT("증오 또는 악의적인 콘텐츠"),
    HARASSMENT_OR_VIOLENCE("괴롭힘 또는 폭력"),
    HARMFUL_OR_DANGEROUS_ACTS("유해하거나 위험한 행위"),
    MISINFORMATION("잘못된 정보"),
    CHILD_ABUSE("아동 학대"),
    TERRORISM_PROMOTION("테러 조장"),
    SPAM_OR_MISLEADING_CONTENT("스팸 또는 혼동을 야기하는 콘텐츠");

    // 설명을 반환하는 메서드
    private final String description;

    // 생성자
    ReportReason(String description) {
        this.description = description;
    }


}
