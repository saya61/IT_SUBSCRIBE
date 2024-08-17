package com.sw.journal.journalcrawlerpublisher.dto;

import com.sw.journal.journalcrawlerpublisher.domain.Category;
import com.sw.journal.journalcrawlerpublisher.domain.Article;
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
// 기사를 우리 사이트에 게시할 때 필요한 데이터를 전송하기 위한 DTO
public class ArticleWithTagsDTO {
    private Long id; // 기사 id
    private String title; // 기사 제목
    private String content; // 기사 내용
    private LocalDateTime postDate; // 기사
    private Category category; // 기사 카테고리
    private String source; // 기사 출처
    private List<Tag> tags; // 기사 태그
    private List<String> imgUrls; // 기사 이미지 URL 리스트

    public static ArticleWithTagsDTO from(Article article, TagService tagService, ImageService imageService) {
        ArticleWithTagsDTO dto = new ArticleWithTagsDTO();
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

    public ArticleWithTagsDTO() {}

    // 크롤링 이벤트 처리 때 사용되는 DTO 생성을 위한 생성자
    // 기사 id, 제목, 카테고리만 사용돼서 다른 필드는 초기화하지 않았음
    public ArticleWithTagsDTO(Long id, String title, Category category) {
        this.id = id;
        this.title = title;
        this.category = category;
    }
}