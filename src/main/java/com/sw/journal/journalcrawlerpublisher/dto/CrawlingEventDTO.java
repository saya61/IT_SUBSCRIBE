package com.sw.journal.journalcrawlerpublisher.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
// Kafka 로 크롤링 이벤트에 대한 메시지를 전송하기 위한 DTO 클래스
public class CrawlingEventDTO {
    private Long categoryId;
    private Long articleId;

    public CrawlingEventDTO(Long categoryId, Long articleId) {
        this.categoryId = categoryId;
        this.articleId = articleId;
    }
}
