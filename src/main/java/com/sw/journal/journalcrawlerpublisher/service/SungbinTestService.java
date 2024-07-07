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
public class SungbinTestService {

    @Autowired
    private OurArticleRepository ourArticleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserFavoriteCategoryRepository userFavoriteCategoryRepository;

    @Autowired
    public SungbinTestService(OurArticleRepository ourArticleRepository) {
        this.ourArticleRepository = ourArticleRepository;
    }

    // 1개의 카테고리로만 검색
    public List<OurArticle> findByCategory(Category category) {
        return ourArticleRepository.findByCategory(category);
    }

    // n개의 카테고리로 검색
    public List<OurArticle> findByCategories(List<Category> categories) {
        return ourArticleRepository.findByCategories(categories);
    }

    // 1개 태그로만 검색
    public List<OurArticle> findByTag(Tag tag) {
        return ourArticleRepository.findByTag(tag);
    }

    // n개 태그로 검색
    public List<OurArticle> findByTags(List<Tag> tags) {
        return ourArticleRepository.findByTags(tags, tags.size());
    }

    // 1개 카테고리, 1개 태그로 검색
    public List<OurArticle> findByCategoryAndTag(Category category, Tag tag) {
        return ourArticleRepository.findByCategoryAndTag(category, tag);
    }

    // 1개 카테고리, n개 태그로 검색
    public List<OurArticle> findByCategoryAndTags(Category category, List<Tag> tags) {
        return ourArticleRepository.findByCategoryAndTags(category, tags, tags.size());
    }

    // n개 카테고리, 1개 태그로 검색
    public List<OurArticle> findByCategoriesAndTag(List<Category> categories, Tag tag) {
        return ourArticleRepository.findByCategoriesAndTag(categories, tag);
    }

    // n개 카테고리, n개 태그로 검색
    public List<OurArticle> findByCategoriesAndTags(List<Category> categories, List<Tag> tags) {
        return ourArticleRepository.findByCategoriesAndTags(categories, tags, tags.size());
    }

    // 유저가 선호하는 카테고리의 기사 검색
    public List<OurArticle> findByUserFavoriteCategories(Member member) {
        List<UserFavoriteCategory> favoriteCategories = userFavoriteCategoryRepository.findByMember(member);
        List<Category> categories = favoriteCategories.stream()
                .map(UserFavoriteCategory::getCategory)
                .collect(Collectors.toList());
        return findByCategories(categories);
    }
}
