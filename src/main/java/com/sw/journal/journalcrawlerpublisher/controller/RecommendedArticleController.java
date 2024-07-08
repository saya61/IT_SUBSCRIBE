package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.domain.OurArticle;
import com.sw.journal.journalcrawlerpublisher.service.RecommendArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test")
public class RecommendedArticleController {

    private final RecommendArticleService recommendArticleService;

    @Autowired
    public RecommendedArticleController(RecommendArticleService recommendArticleService) {
        this.recommendArticleService = recommendArticleService;
    }

    // 유저 선호 카테고리 기사 검색
    @GetMapping("/user/{userId}/favorites")
    public List<OurArticle> getArticlesByUserFavoriteCategories(@PathVariable Long userId) {
        Member member = new Member();
        member.setId(userId);
        return recommendArticleService.findByUserFavoriteCategories(member);
    }
}
