package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.domain.OurArticle;
import com.sw.journal.journalcrawlerpublisher.dto.OurArticleWithTagsDTO;
import com.sw.journal.journalcrawlerpublisher.domain.Image;
import com.sw.journal.journalcrawlerpublisher.service.ArticleRankService;
import com.sw.journal.journalcrawlerpublisher.service.ImageService;
import com.sw.journal.journalcrawlerpublisher.service.OurArticleService;
import com.sw.journal.journalcrawlerpublisher.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rank")
public class ArticleRankController {

    // 필드 주입에서 생성자 주입으로 변경
    private final ArticleRankService articleRankService;
    private final OurArticleService ourArticleService;
    private final TagService tagService;
    private final ImageService imageService;



    @PostMapping("/increment-article-count/{articleId}")
    public void incrementArticleCount(@PathVariable Long articleId) {
        articleRankService.increaseArticleCount(articleId);
    }

    @GetMapping("/top-articles")
    public List<OurArticleWithTagsDTO> getTopArticles() {
        Set<String> topTenArticleIds = articleRankService.getTopTenArticle();
        Set<Long> topTenArticleIdsLong = topTenArticleIds.stream()
                .map(Long::parseLong)
                .collect(Collectors.toSet());

        return topTenArticleIdsLong.stream()
                .map(id -> {
                    Optional<OurArticle> articleOptional = ourArticleService.findById(id);
                    if (articleOptional.isPresent()) {
                        OurArticle article = articleOptional.get();
                        return OurArticleWithTagsDTO.from(article, tagService, imageService);
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}