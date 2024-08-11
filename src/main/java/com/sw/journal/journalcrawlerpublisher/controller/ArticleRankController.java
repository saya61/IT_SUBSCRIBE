package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.domain.OurArticle;
import com.sw.journal.journalcrawlerpublisher.dto.OurArticleWithTagsDTO;
import com.sw.journal.journalcrawlerpublisher.domain.Image;
import com.sw.journal.journalcrawlerpublisher.service.ArticleRankService;
import com.sw.journal.journalcrawlerpublisher.service.ImageService;
import com.sw.journal.journalcrawlerpublisher.service.OurArticleService;
import com.sw.journal.journalcrawlerpublisher.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rank")
public class ArticleRankController {

    @Autowired
    private ArticleRankService articleRankService;
    @Autowired
    private OurArticleService ourArticleService;
    @Autowired
    private TagService tagService;
    @Autowired
    private ImageService imageService;

    // 기사 조회수 증가
    @PostMapping("/increment-article-count/{articleId}")
    public void incrementArticleCount(@PathVariable Long articleId) {
        articleRankService.increaseArticleCount(articleId);
    }

    // 랭킹 10위 기사 표시
    @GetMapping("/top-articles")
    public List<OurArticleWithTagsDTO> getTopArticles() {
        // Top 10 기사 ID를 가져옴
        Set<String> topTenArticleIds = articleRankService.getTopTenArticle();

        // String 타입인 기사 ID를 Long 타입으로 변환
        Set<Long> topTenArticleIdsLong = topTenArticleIds.stream()
                .map(Long::parseLong)
                .collect(Collectors.toSet());

        // 각 기사 ID를 OurArticleWithTagsDTO 객체로 변환한 후 리스트로 반환
        return topTenArticleIdsLong.stream()
                .map(id -> {
                    // 기사 ID로 기사 조회
                    Optional<OurArticle> articleOptional = ourArticleService.findById(id);
                    // 기사가 존재할 경우
                    if (articleOptional.isPresent()) {
                        // 기사 정보를 가져옴
                        OurArticle article = articleOptional.get();
                        // OurArticleWithTagsDTO 객체를 생성하고 DTO에 기사 정보를 설정
                        OurArticleWithTagsDTO dto = new OurArticleWithTagsDTO();
                        dto.setId(article.getId());
                        dto.setTitle(article.getTitle());
                        dto.setContent(article.getContent());
                        dto.setPostDate(article.getPostDate());
                        dto.setCategory(article.getCategory());
                        dto.setSource(article.getSource());

                        // 태그를 조회하여 DTO에 설정
                        dto.setTags(tagService.findByArticle(article));

                        // 이미지 URL을 조회하여 DTO에 설정
                        dto.setImgUrls(imageService.findByArticle(article).stream()
                                .map(Image::getImgUrl)
                                .collect(Collectors.toList()));

                        return dto;
                    } else {  // 기사가 존재하지 않을 경우 null 반환
                        return null;
                    }
                })
                // null 값은 필터링하여 제외
                .filter(article -> article != null)
                .collect(Collectors.toList()); // 리스트로 반환
    }
}
