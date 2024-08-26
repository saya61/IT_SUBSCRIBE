package com.sw.journal.journalcrawlerpublisher.dto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class MemberViewArticleDTO {
    private Long memberId;
    private Long articleId;
    private String memberIp;
    private String userAgent;
    private String referrer;
}
