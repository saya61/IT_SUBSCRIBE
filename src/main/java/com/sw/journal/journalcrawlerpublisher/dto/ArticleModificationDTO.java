package com.sw.journal.journalcrawlerpublisher.dto;

import com.sw.journal.journalcrawlerpublisher.domain.TagArticle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class ArticleModificationDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String title;
        private String content;
        private List<String> tags;
    }
}
