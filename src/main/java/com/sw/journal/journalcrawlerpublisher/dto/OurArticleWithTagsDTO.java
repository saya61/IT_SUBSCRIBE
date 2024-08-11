package com.sw.journal.journalcrawlerpublisher.dto;

import com.sw.journal.journalcrawlerpublisher.domain.Category;
import com.sw.journal.journalcrawlerpublisher.domain.Tag;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
// 기사를 우리 사이트에 게시할 때 필요한 데이터를 전송하기 위한 DTO
public class OurArticleWithTagsDTO {
    private Long id; // 기사 id
    private String title; // 기사 제목
    private String content; // 기사 내용
    private LocalDateTime postDate; // 기사
    private Category category; // 기사 카테고리
    private String source; // 기사 출처
    private List<Tag> tags; // 기사 태그
    private List<String> imgUrls; // 기사 이미지 URL 리스트
}