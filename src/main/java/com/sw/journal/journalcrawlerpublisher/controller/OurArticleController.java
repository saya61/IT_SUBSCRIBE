package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.domain.Category;
import com.sw.journal.journalcrawlerpublisher.domain.OurArticle;
import com.sw.journal.journalcrawlerpublisher.domain.Tag;
import com.sw.journal.journalcrawlerpublisher.dto.CommentDTO;
import com.sw.journal.journalcrawlerpublisher.dto.OurArticleWithTagsDTO;
import com.sw.journal.journalcrawlerpublisher.service.CommentService;
import com.sw.journal.journalcrawlerpublisher.service.ImageService;
import com.sw.journal.journalcrawlerpublisher.service.OurArticleService;
import com.sw.journal.journalcrawlerpublisher.service.TagService;
import com.sw.journal.journalcrawlerpublisher.domain.Image;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/article")
public class OurArticleController {

    private final OurArticleService ourArticleService;
    private final TagService tagService;
    private final ImageService imageService;
    private final CommentService commentService;


    // 전체 기사 보기 (태그 포함)
    @GetMapping("/all")
    public Page<OurArticleWithTagsDTO> getAllArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OurArticle> articlePage = ourArticleService.findAll(pageable);

        List<OurArticleWithTagsDTO> articleDTOs = articlePage.getContent().stream()
                .map(article -> {
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
                }).collect(Collectors.toList());

        return new PageImpl<>(articleDTOs, pageable, articlePage.getTotalElements());
    }

    // 카테고리별 페이지네이션된 기사 검색 (태그 포함)
    @GetMapping("/category/{categoryId}")
    public Page<OurArticleWithTagsDTO> getArticlesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Category category = new Category();
        category.setId(categoryId);
        Pageable pageable = PageRequest.of(page, size);
        Page<OurArticle> articlePage = ourArticleService.findByCategory(category, pageable);

        List<OurArticleWithTagsDTO> articleDTOs = articlePage.getContent().stream()
                .map(article -> {
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
                }).collect(Collectors.toList());

        return new PageImpl<>(articleDTOs, pageable, articlePage.getTotalElements());
    }

    @GetMapping("/article/{articleId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByArticle(@PathVariable Long articleId) {
        List<CommentDTO> comments = commentService.getCommentsByArticle(articleId);
        return ResponseEntity.ok(comments);
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
