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
                    } else {
                        return null;
                    }
                })
                .filter(article -> article != null)
                .collect(Collectors.toList());
    }
}