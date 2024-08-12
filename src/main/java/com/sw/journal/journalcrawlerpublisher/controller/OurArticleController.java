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
import org.springframework.data.domain.*;
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

    // 전체 기사 조회 (태그 포함)
    @GetMapping("/all")
    public Page<OurArticleWithTagsDTO> getAllArticles(
            @RequestParam(defaultValue = "0") int page, // 페이지 번호 (기본값 0)
            @RequestParam(defaultValue = "12") int size) { // 페이지 크기 (기본값 12)
        // 페이지 번호, 페이지 크기를 지정한 페이지네이션 객체 생성
        Pageable pageable = PageRequest.of(page, size);
        // 페이지네이션된 기사 목록 조회
        Page<OurArticle> articlePage = ourArticleService.findAll(pageable);

        // 조회된 기사를 DTO 로 변환
        List<OurArticleWithTagsDTO> articleDTOs = articlePage.getContent().stream()
                .map(article -> OurArticleWithTagsDTO.from(article, tagService, imageService))
                .collect(Collectors.toList());

        // DTO 리스트, 페이지네이션, 전체 기사 수를 포함하는 페이지 객체를 생성하여 반환
        return new PageImpl<>(articleDTOs, pageable, articlePage.getTotalElements());
    }

    // 카테고리별 기사 조회 (태그 포함)
    @GetMapping("/category/{categoryId}")
    public Page<OurArticleWithTagsDTO> getArticlesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page, // 페이지 번호 (기본값 0)
            @RequestParam(defaultValue = "12") int size) { // 페이지 크기 (기본값 12)
        // 카테고리 객체 생성
        Category category = new Category();
        category.setId(categoryId); // 카테고리 ID 설정
        // 페이지 번호, 페이지 크기를 지정한 페이지네이션 객체 생성
        Pageable pageable = PageRequest.of(page, size);
        // 카테고리별 기사 조회
        Page<OurArticle> articlePage = ourArticleService.findByCategory(category, pageable);

        // 조회된 기사를 DTO 로 변환
        List<OurArticleWithTagsDTO> articleDTOs = articlePage.getContent().stream()
                .map(article -> OurArticleWithTagsDTO.from(article, tagService, imageService))
                .collect(Collectors.toList());

        // DTO 리스트, 페이지네이션, 전체 기사 수를 포함하는 페이지 객체를 생성하여 반환
        return new PageImpl<>(articleDTOs, pageable, articlePage.getTotalElements());
    }

    // 최근 게시된 기사 18개 조회
    @GetMapping("/recent")
    public List<OurArticleWithTagsDTO> getRecentArticles() {
        // 최근 게시된 기사 18개를 조회하기 위한 페이지네이션 객체 생성
        Pageable pageable = PageRequest.of(0, 18, Sort.by(Sort.Direction.DESC, "postDate"));
        // 최근 게시된 기사 조회
        List<OurArticle> articles = ourArticleService.findAll(pageable).getContent();

        // 조회된 기사를 DTO 로 변환
        return articles.stream()
                .map(article -> OurArticleWithTagsDTO.from(article, tagService, imageService))
                .collect(Collectors.toList());
    }

    // 특정 기사에 대한 모든 댓글 조회
    @GetMapping("/article/{articleId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByArticle(@PathVariable Long articleId) {
        // 기사 ID로 댓글 리스트 조회
        List<CommentDTO> comments = commentService.getCommentsByArticle(articleId);
        // 조회된 댓글 리스트를 응답으로 반환
        return ResponseEntity.ok(comments);
    }

    // 카테고리 n개로 기사 조회
    @PostMapping("/categories")
    public List<OurArticle> getArticlesByCategories(@RequestBody List<Long> categoryIds) {
        // 카테고리 리스트 생성
        // categoryIds 리스트의 각 ID를 Category 객체로 변환한 후 카테고리 리스트에 저장
        List<Category> categories = categoryIds.stream().map(id -> {
            Category category = new Category(); // 카테고리 객체 생성
            category.setId(id); // 카테고리 ID 설정
            return category; // 생성된 카테고리 반환
        }).toList(); // 카테고리 리스트로 변환
        return ourArticleService.findByCategories(categories); // 카테고리 리스트로 기사 조회
    }

    // 태그 1개로 기사 조회
    @GetMapping("/tag/{tagCode}")
    public List<OurArticle> getArticlesByTag(@PathVariable Long tagCode) {
        Tag tag = new Tag(); // 태그 객체 생성
        tag.setId(tagCode); // 태그 ID 설정
        return ourArticleService.findByTag(tag); // 태그로 기사 조회
    }

    // 태그 n개로 기사 조회
    @PostMapping("/tags")
    public List<OurArticle> getArticlesByTags(@RequestBody List<Long> tagCodes) {
        // 태그 리스트 생성
        // tagCodes 리스트의 각 code 를 Tag 객체로 변환한 후 태그 리스트에 저장
        List<Tag> tags = tagCodes.stream().map(code -> {
            Tag tag = new Tag(); // 태그 객체 생성
            tag.setId(code); // 태그 ID 설정
            return tag; // 생성된 태그 반환
        }).toList(); // 태그 리스트로 변환
        return ourArticleService.findByTags(tags); // 태그 리스트로 기사 조회
    }

    // 카테고리 1개, 태그 1개로 기사 조회
    @GetMapping("/category/{categoryId}/tag/{tagCode}")
    public List<OurArticle> getArticlesByCategoryAndTag(@PathVariable Long categoryId, @PathVariable Long tagCode) {
        Category category = new Category(); // 카테고리 객체 생성
        category.setId(categoryId); // 카테고리 ID 설정
        Tag tag = new Tag(); // 태그 객체 생성
        tag.setId(tagCode); // 태그 ID 설정
        return ourArticleService.findByCategoryAndTag(category, tag); // 카테고리와 태그로 기사 조회
    }

    // 카테고리 1개, 태그 n개로 기사 조회
    @PostMapping("/category/{categoryId}/tags")
    public List<OurArticle> getArticlesByCategoryAndTags(@PathVariable Long categoryId, @RequestBody List<Long> tagCodes) {
        Category category = new Category(); // 카테고리 객체 생성
        category.setId(categoryId); // 카테고리 ID 설정
        // 태그 리스트 생성
        // tagCodes 리스트의 각 code 를 Tag 객체로 변환한 후 태그 리스트에 저장
        List<Tag> tags = tagCodes.stream().map(code -> {
            Tag tag = new Tag(); // 태그 객체 생성
            tag.setId(code); // 태그 ID 설정
            return tag; // 생성된 태그 반환
        }).toList(); // 태그 리스트로 변환
        return ourArticleService.findByCategoryAndTags(category, tags); // 카테고리와 태그 리스트로 기사 조회
    }

    // 카테고리 n개, 태그 1개로 기사 조회
    @PostMapping("/categories/tag/{tagCode}")
    public List<OurArticle> getArticlesByCategoriesAndTag(@RequestBody List<Long> categoryIds, @PathVariable Long tagCode) {
        // 카테고리 리스트 생성
        // categoryIds 리스트의 각 ID를 Category 객체로 변환한 후 카테고리 리스트에 저장
        List<Category> categories = categoryIds.stream().map(id -> {
            Category category = new Category(); // 카테고리 객체 생성
            category.setId(id); // 카테고리 ID 설정
            return category; // 생성된 카테고리 반환
        }).toList(); // 카테고리 리스트로 변환
        Tag tag = new Tag(); // 태그 객체 생성
        tag.setId(tagCode); // 태그 ID 설정
        return ourArticleService.findByCategoriesAndTag(categories, tag); // 카테고리 리스트와 태그로 기사 조회
    }

    // 카테고리 n개, 태그 n개로 기사 조회
    @PostMapping("/categories/tags")
    public List<OurArticle> getArticlesByCategoriesAndTags(@RequestBody CategoryTagRequest request) {
        // 카테고리 리스트 생성
        // CategoryTagRequest 객체에서 가져온 카테고리 ID 리스트의 각 ID를 Category 객체로 변환한 후 카테고리 리스트에 저장
        List<Category> categories = request.getCategoryIds().stream().map(id -> {
            Category category = new Category(); // 카테고리 객체 생성
            category.setId(id); // 카테고리 ID 설정
            return category; // 생성된 카테고리 반환
        }).toList(); // 카테고리 리스트로 변환
        // 태그 리스트 생성
        // CategoryTagRequest 객체에서 가져온 태그 리스트의 각 code 를 Tag 객체로 변환하여 태그 리스트에 저장
        List<Tag> tags = request.getTagCodes().stream().map(code -> {
            Tag tag = new Tag(); // 태그 객체 생성
            tag.setId(code); // 태그 ID 설정
            return tag; // 생성된 태그 반환
        }).toList(); // 태그 리스트로 변환
        return ourArticleService.findByCategoriesAndTags(categories, tags); // 카테고리 리스트와 태그 리스트로 기사 조회
    }

    // 카테고리 ID 리스트와 태그 리스트를 전달하기 위한 DTO 클래스
    @Getter @Setter
    public static class CategoryTagRequest {
        private List<Long> categoryIds; // 카테고리 ID 리스트
        private List<Long> tagCodes; // 태그 리스트
    }
}
