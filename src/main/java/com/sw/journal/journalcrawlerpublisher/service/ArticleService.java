package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.domain.Category;
import com.sw.journal.journalcrawlerpublisher.domain.Tag;
import com.sw.journal.journalcrawlerpublisher.repository.ArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.CategoryRepository;
import com.sw.journal.journalcrawlerpublisher.repository.TagArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.TagRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@RequiredArgsConstructor
@Service
public class ArticleService {

    // 필드 주입에서 생성자 주입으로 변경
    private final ArticleRepository articleRepository;

    private final CategoryRepository categoryRepository;

    private final TagRepository tagRepository;

    private final TagArticleRepository tagArticleRepository;

    public Optional<Article> findById(Long id) {
        return articleRepository.findById(id);
    }

    // 카테고리별 페이지네이션된 기사 검색
    public Page<Article> findByCategory(Category category, Pageable pageable) {
        return articleRepository.findByCategory(category, pageable);
    }

    public Page<Article> findAll(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }

    // 1개의 카테고리로만 검색
    public List<Article> findByCategory(Category category) {
        return articleRepository.findByCategory(category);
    }


    // n개의 카테고리로 검색
    public List<Article> findByCategories(List<Category> categories) {
        return articleRepository.findByCategories(categories);
    }

    // 1개 태그로만 검색
    public List<Article> findByTag(Tag tag) {
        return articleRepository.findByTag(tag);
    }

    // n개 태그로 검색
    public List<Article> findByTags(List<Tag> tags) {
        return articleRepository.findByTags(tags, tags.size());
    }

    // 1개 카테고리, 1개 태그로 검색
    public List<Article> findByCategoryAndTag(Category category, Tag tag) {
        return articleRepository.findByCategoryAndTag(category, tag);
    }

    // 1개 카테고리, n개 태그로 검색
    public List<Article> findByCategoryAndTags(Category category, List<Tag> tags) {
        return articleRepository.findByCategoryAndTags(category, tags, tags.size());
    }

    // n개 카테고리, 1개 태그로 검색
    public List<Article> findByCategoriesAndTag(List<Category> categories, Tag tag) {
        return articleRepository.findByCategoriesAndTag(categories, tag);
    }

    // n개 카테고리, n개 태그로 검색
    public List<Article> findByCategoriesAndTags(List<Category> categories, List<Tag> tags) {
        return articleRepository.findByCategoriesAndTags(categories, tags, tags.size());
    }

}
