package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.domain.Category;
import com.sw.journal.journalcrawlerpublisher.domain.OurArticle;
import com.sw.journal.journalcrawlerpublisher.domain.Tag;
import com.sw.journal.journalcrawlerpublisher.service.OurArticleService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/article")
public class OurArticleController {

    private final OurArticleService ourArticleService;

    @Autowired
    public OurArticleController(OurArticleService ourArticleService) {
        this.ourArticleService = ourArticleService;
    }

    // 카테고리 1개로 기사 검색
    @GetMapping("/category/{categoryId}")
    public List<OurArticle> getArticlesByCategory(@PathVariable Long categoryId) {
        Category category = new Category();
        category.setId(categoryId);
        return ourArticleService.findByCategory(category);
    }

    // 카테고리 n개로 검색
    @PostMapping("/categories")
    public List<OurArticle> getArticlesByCategories(@RequestBody List<Long> categoryIds) {
        List<Category> categories = categoryIds.stream().map(id -> {
            Category category = new Category();
            category.setId(id);
            return category;
        }).toList();
        return ourArticleService.findByCategories(categories);
    }

    // 태그 1개로 기사 검색
    @GetMapping("/tag/{tagCode}")
    public List<OurArticle> getArticlesByTag(@PathVariable Long tagCode) {
        Tag tag = new Tag();
        tag.setId(tagCode);
        return ourArticleService.findByTag(tag);
    }

    // n개 태그로 검색
    @PostMapping("/tags")
    public List<OurArticle> getArticlesByTags(@RequestBody List<Long> tagCodes) {
        List<Tag> tags = tagCodes.stream().map(code -> {
            Tag tag = new Tag();
            tag.setId(code);
            return tag;
        }).toList();
        return ourArticleService.findByTags(tags);
    }

    // 카테고리 1개, 태그 1개로 기사 검색
    @GetMapping("/category/{categoryId}/tag/{tagCode}")
    public List<OurArticle> getArticlesByCategoryAndTag(@PathVariable Long categoryId, @PathVariable Long tagCode) {
        Category category = new Category();
        category.setId(categoryId);
        Tag tag = new Tag();
        tag.setId(tagCode);
        return ourArticleService.findByCategoryAndTag(category, tag);
    }

    // 1개 카테고리, n개 태그로 검색
    @PostMapping("/category/{categoryId}/tags")
    public List<OurArticle> getArticlesByCategoryAndTags(@PathVariable Long categoryId, @RequestBody List<Long> tagCodes) {
        Category category = new Category();
        category.setId(categoryId);
        List<Tag> tags = tagCodes.stream().map(code -> {
            Tag tag = new Tag();
            tag.setId(code);
            return tag;
        }).toList();
        return ourArticleService.findByCategoryAndTags(category, tags);
    }

    // n개 카테고리, 1개 태그로 검색
    @PostMapping("/categories/tag/{tagCode}")
    public List<OurArticle> getArticlesByCategoriesAndTag(@RequestBody List<Long> categoryIds, @PathVariable Long tagCode) {
        List<Category> categories = categoryIds.stream().map(id -> {
            Category category = new Category();
            category.setId(id);
            return category;
        }).toList();
        Tag tag = new Tag();
        tag.setId(tagCode);
        return ourArticleService.findByCategoriesAndTag(categories, tag);
    }

    // n개 카테고리, n개 태그로 검색
    @PostMapping("/categories/tags")
    public List<OurArticle> getArticlesByCategoriesAndTags(@RequestBody CategoryTagRequest request) {
        List<Category> categories = request.getCategoryIds().stream().map(id -> {
            Category category = new Category();
            category.setId(id);
            return category;
        }).toList();
        List<Tag> tags = request.getTagCodes().stream().map(code -> {
            Tag tag = new Tag();
            tag.setId(code);
            return tag;
        }).toList();
        return ourArticleService.findByCategoriesAndTags(categories, tags);
    }

    @Getter @Setter
    public static class CategoryTagRequest {
        private List<Long> categoryIds;
        private List<Long> tagCodes;
    }
}
