package com.sw.journal.journalcrawlerpublisher.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.domain.Category;
import com.sw.journal.journalcrawlerpublisher.domain.Image;
import com.sw.journal.journalcrawlerpublisher.domain.Tag;
import com.sw.journal.journalcrawlerpublisher.repository.CategoryRepository;
import com.sw.journal.journalcrawlerpublisher.service.ArticleService;
import com.sw.journal.journalcrawlerpublisher.service.ImageService;
import com.sw.journal.journalcrawlerpublisher.service.TagService;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
// 검색에 사용할 DTO
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ArticleSearchDTO {
    private CategoryRepository categoryRepository;
    private Long id; // 기사 id
    private String title; // 기사 제목
    private String content; // 기사 내용
    private LocalDateTime postDate; // 기사
    private String category; // 기사 카테고리
    private String source; // 기사 출처
    private List<Tag> tags; // 기사 태그
    private List<String> imgUrls; // 기사 이미지 URL 리스트

    public static ArticleSearchDTO from(Article article, TagService tagService, ImageService imageService) {
        ArticleSearchDTO dto = new ArticleSearchDTO();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setContent(article.getContent());
        dto.setPostDate(article.getPostDate());
        dto.setCategory(dto.getCategory());
        dto.setSource(article.getSource());
        dto.setTags(tagService.findByArticle(article));
        dto.setImgUrls(imageService.findByArticle(article).stream()
                .map(Image::getImgUrl)
                .collect(Collectors.toList()));
        return dto;
    }
}
