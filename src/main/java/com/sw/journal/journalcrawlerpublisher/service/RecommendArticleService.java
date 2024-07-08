package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.*;
import com.sw.journal.journalcrawlerpublisher.repository.CategoryRepository;
import com.sw.journal.journalcrawlerpublisher.repository.OurArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.TagRepository;
import com.sw.journal.journalcrawlerpublisher.repository.UserFavoriteCategoryRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// wild-mantle 2024-07-08
@Getter @Setter
@Service
public class RecommendArticleService {

    @Autowired
    private OurArticleRepository ourArticleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserFavoriteCategoryRepository userFavoriteCategoryRepository;

    private OurArticleService ourArticleService;

    // 유저가 선호하는 카테고리의 기사 검색
    public List<OurArticle> findByUserFavoriteCategories(Member member) {
        List<UserFavoriteCategory> favoriteCategories = userFavoriteCategoryRepository.findByMember(member);
        List<Category> categories = favoriteCategories.stream()
                .map(UserFavoriteCategory::getCategory)
                .collect(Collectors.toList());
        return ourArticleService.findByCategories(categories);
    }
}
