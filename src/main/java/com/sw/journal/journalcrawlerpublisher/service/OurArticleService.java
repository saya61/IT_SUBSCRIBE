package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.Category;
import com.sw.journal.journalcrawlerpublisher.domain.OurArticle;
import com.sw.journal.journalcrawlerpublisher.domain.Tag;
import com.sw.journal.journalcrawlerpublisher.repository.CategoryRepository;
import com.sw.journal.journalcrawlerpublisher.repository.OurArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.TagArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.TagRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@RequiredArgsConstructor
@Service
public class OurArticleService {

    // 필드 주입에서 생성자 주입으로 변경
    private final OurArticleRepository ourArticleRepository;

    private final CategoryRepository categoryRepository;

    private final TagRepository tagRepository;

    private final TagArticleRepository tagArticleRepository;

    public Optional<OurArticle> findById(Long id) {
        return ourArticleRepository.findById(id);
    }

    // 카테고리별 페이지네이션된 기사 검색
    public Page<OurArticle> findByCategory(Category category, Pageable pageable) {
        return ourArticleRepository.findByCategory(category, pageable);
    }

    public Page<OurArticle> findAll(Pageable pageable) {
        return ourArticleRepository.findAll(pageable);
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
//
//
//
//    // n개 태그로 검색
//    public List<OurArticle> findByTags(List<Tag> tags) {
//        return ourArticleRepository.findByTags(tags, tags.size());
//    }
//
//    // 1개 카테고리, 1개 태그로 검색
//    public List<OurArticle> findByCategoryAndTag(Category category, Tag tag) {
//        return ourArticleRepository.findByCategoryAndTag(category, tag);
//    }
//
//    // 1개 카테고리, n개 태그로 검색
//    public List<OurArticle> findByCategoryAndTags(Category category, List<Tag> tags) {
//        return ourArticleRepository.findByCategoryAndTags(category, tags, tags.size());
//    }
//
//    // n개 카테고리, 1개 태그로 검색
//    public List<OurArticle> findByCategoriesAndTag(List<Category> categories, Tag tag) {
//        return ourArticleRepository.findByCategoriesAndTag(categories, tag);
//    }
//
//    // n개 카테고리, n개 태그로 검색
//    public List<OurArticle> findByCategoriesAndTags(List<Category> categories, List<Tag> tags) {
//        return ourArticleRepository.findByCategoriesAndTags(categories, tags, tags.size());
//    }

}
