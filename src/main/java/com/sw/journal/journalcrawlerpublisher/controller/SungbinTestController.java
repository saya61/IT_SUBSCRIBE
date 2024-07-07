package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.domain.Category;
import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.domain.OurArticle;
import com.sw.journal.journalcrawlerpublisher.domain.Tag;
import com.sw.journal.journalcrawlerpublisher.service.SungbinTestService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test")
public class SungbinTestController {

    private final SungbinTestService sungbinTestService;

    @Autowired
    public SungbinTestController(SungbinTestService sungbinTestService) {
        this.sungbinTestService = sungbinTestService;
    }

    // 유저 선호 카테고리 기사 검색
    @GetMapping("/user/{userId}/favorites")
    public List<OurArticle> getArticlesByUserFavoriteCategories(@PathVariable Long userId) {
        Member member = new Member();
        member.setId(userId);
        return sungbinTestService.findByUserFavoriteCategories(member);
    }
}
