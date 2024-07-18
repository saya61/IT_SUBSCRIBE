package com.sw.journal.journalcrawlerpublisher.dto;

import com.sw.journal.journalcrawlerpublisher.domain.Category;
import com.sw.journal.journalcrawlerpublisher.domain.Tag;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OurArticleWithTagsDTO {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime postDate;
    private Category category;
    private String source;
    private List<Tag> tags;
}