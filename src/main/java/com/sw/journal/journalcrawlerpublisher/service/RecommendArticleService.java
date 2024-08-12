package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.*;
import com.sw.journal.journalcrawlerpublisher.repository.CategoryRepository;
import com.sw.journal.journalcrawlerpublisher.repository.OurArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.TagRepository;
import com.sw.journal.journalcrawlerpublisher.repository.UserFavoriteCategoryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// wild-mantle 2024-07-08
@Getter @Setter
@RequiredArgsConstructor
@Service
public class RecommendArticleService {
    // 0809 wildmantle : 필드 주입에서 생성자 주입으로 변경
    private final OurArticleRepository ourArticleRepository;

    private final CategoryRepository categoryRepository;

    private final TagRepository tagRepository;

    private final UserFavoriteCategoryRepository userFavoriteCategoryRepository;

    private final OurArticleService ourArticleService;

    public List<OurArticle> findRecentArticles(int limit) {
        return ourArticleRepository.findAll(PageRequest.of(0, limit)).getContent();
    }

    public List<OurArticle> findByUserFavoriteCategories(Member member) {
        List<UserFavoriteCategory> favoriteCategories = userFavoriteCategoryRepository.findByMember(member);
        List<Category> categories = favoriteCategories.stream()
                .map(UserFavoriteCategory::getCategory)
                .collect(Collectors.toList());
        return ourArticleRepository.findByCategories(categories);
    }

    // 0809 wildmantle : 유저 정보 기반 기사 추천
//    public List<OurArticle> findTopByUserFavoriteCategoriesOrderByPostDateDesc(Member member, int limit) {
//        List<UserFavoriteCategory> favoriteCategories = userFavoriteCategoryRepository.findByMember(member);
//        List<Category> categories = favoriteCategories.stream()
//                .map(UserFavoriteCategory::getCategory)
//                .collect(Collectors.toList());
//        return ourArticleRepository.findTopByCategoriesOrderByPostDate(categories, PageRequest.of(0, limit));
//    }
}