package com.sw.journal.journalcrawlerpublisher.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.annotation.Bean;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class CrawlingEventDTO {
    private Long categoryId;
    private Long articleId;

    public CrawlingEventDTO(Long categoryId, Long articleId) {
        this.categoryId = categoryId;
        this.articleId = articleId;
    }
}
