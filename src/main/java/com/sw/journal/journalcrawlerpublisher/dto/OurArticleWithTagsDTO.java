package com.sw.journal.journalcrawlerpublisher.dto;

import com.sw.journal.journalcrawlerpublisher.domain.Category;
import com.sw.journal.journalcrawlerpublisher.domain.OurArticle;
import com.sw.journal.journalcrawlerpublisher.domain.Tag;
import com.sw.journal.journalcrawlerpublisher.domain.Image;

import com.sw.journal.journalcrawlerpublisher.service.ImageService;
import com.sw.journal.journalcrawlerpublisher.service.TagService;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private List<String> imgUrls; // 여러 개의 이미지 URL 추가

    public static OurArticleWithTagsDTO from(OurArticle article, TagService tagService, ImageService imageService) {
        OurArticleWithTagsDTO dto = new OurArticleWithTagsDTO();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setContent(article.getContent());
        dto.setPostDate(article.getPostDate());
        dto.setCategory(article.getCategory());
        dto.setSource(article.getSource());
        dto.setTags(tagService.findByArticle(article));
        dto.setImgUrls(imageService.findByArticle(article).stream()
                .map(Image::getImgUrl)
                .collect(Collectors.toList()));
        return dto;
    }
}