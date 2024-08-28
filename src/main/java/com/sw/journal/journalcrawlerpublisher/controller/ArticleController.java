package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.domain.*;
import com.sw.journal.journalcrawlerpublisher.dto.ArticleWithTagsDTO;
import com.sw.journal.journalcrawlerpublisher.dto.CommentDTO;
import com.sw.journal.journalcrawlerpublisher.dto.MemberViewArticleDTO;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import com.sw.journal.journalcrawlerpublisher.service.CommentService;
import com.sw.journal.journalcrawlerpublisher.service.ImageService;
import com.sw.journal.journalcrawlerpublisher.service.ArticleService;
import com.sw.journal.journalcrawlerpublisher.service.TagService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.util.*;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/article")
public class ArticleController {

    private final ArticleService articleService;
    private final TagService tagService;
    private final ImageService imageService;
    private final CommentService commentService;
    private final MemberRepository memberRepository;

//     전체 기사 보기 (태그 포함)
//    @GetMapping("/all")
//    public Page<OurArticleWithTagsDTO> getAllArticles(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "12") int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        Page<OurArticle> articlePage = ourArticleService.findAll(pageable);
//
//        List<OurArticleWithTagsDTO> articleDTOs = articlePage.getContent().stream()
//                .map(article -> {
//                    OurArticleWithTagsDTO dto = new OurArticleWithTagsDTO();
//                    dto.setId(article.getId());
//                    dto.setTitle(article.getTitle());
//                    dto.setContent(article.getContent());
//                    dto.setPostDate(article.getPostDate());
//                    dto.setCategory(article.getCategory());
//                    dto.setSource(article.getSource());
//                    dto.setTags(tagService.findByArticle(article));
//                    dto.setImgUrls(imageService.findByArticle(article).stream()
//                            .map(Image::getImgUrl)
//                            .collect(Collectors.toList()));
//                    return dto;
//                }).collect(Collectors.toList());
//
//        return new PageImpl<>(articleDTOs, pageable, articlePage.getTotalElements());
//    }

    // 카테고리별 기사 조회 (태그 포함)
    @GetMapping("/category/{categoryId}")
    public Page<ArticleWithTagsDTO> getArticlesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page, // 페이지 번호 (기본값 0)
            @RequestParam(defaultValue = "12") int size) { // 페이지 크기 (기본값 12)
        // 카테고리 객체 생성
        Category category = new Category();
        category.setId(categoryId); // 카테고리 ID 설정
        // 페이지 번호, 페이지 크기를 지정한 페이지네이션 객체 생성
        Pageable pageable = PageRequest.of(page, size);
        // 카테고리별 기사 조회
        Page<Article> articlePage = articleService.findByCategory(category, pageable);

        // 조회된 기사를 DTO 로 변환
        List<ArticleWithTagsDTO> articleDTOs = articlePage.getContent().stream()
                .map(article -> ArticleWithTagsDTO.from(article, tagService, imageService))
                .collect(Collectors.toList());

        // DTO 리스트, 페이지네이션, 전체 기사 수를 포함하는 페이지 객체를 생성하여 반환
        return new PageImpl<>(articleDTOs, pageable, articlePage.getTotalElements());
    }

    // 최근 게시된 기사 18개 조회
    @GetMapping("/recent")
    public List<ArticleWithTagsDTO> getRecentArticles() {
        // 최근 게시된 기사 18개를 조회하기 위한 페이지네이션 객체 생성
        Pageable pageable = PageRequest.of(0, 18, Sort.by(Sort.Direction.DESC, "postDate"));
        // 최근 게시된 기사 조회
        List<Article> articles = articleService.findAll(pageable).getContent();

        // 조회된 기사를 DTO 로 변환
        return articles.stream()
                .map(article -> ArticleWithTagsDTO.from(article, tagService, imageService))
                .collect(Collectors.toList());
    }

    @GetMapping("/view/{articleId}")
    public ResponseEntity<String> articleLogger(@PathVariable Long articleId, HttpServletRequest request) {
        // 현재 로그인한 사용자 정보를 가져옴
        // 사용자를 React에서 저장하지 않음 따라서 파라미터로 받지 못함
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 로그인한 사용자의 id를 가져옴
        String currentUsername = authentication.getName();
        // 사용자 id로 Member 객체 조회
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 사용자가 존재하지 않는 경우 401 Unauthorized 응답 반환
        if (member.isEmpty()) {
            return ResponseEntity.badRequest().body("사용자 없음");
        }
        // 조회된 Member 객체에서 정보 추출
        Member currentMember = member.get();

        MemberViewArticleDTO mva = new MemberViewArticleDTO();
        mva.setMemberId(currentMember.getId());
        mva.setArticleId(articleId);
        mva.setRequest(request);

        return ResponseEntity.ok("기사 조회 로깅");
    }

    // 카테고리 n개로 기사 조회
    @PostMapping("/categories")
    public List<Article> getArticlesByCategories(@RequestBody List<Long> categoryIds) {
        // 카테고리 리스트 생성
        // categoryIds 리스트의 각 ID를 Category 객체로 변환한 후 카테고리 리스트에 저장
        List<Category> categories = categoryIds.stream().map(id -> {
            Category category = new Category(); // 카테고리 객체 생성
            category.setId(id); // 카테고리 ID 설정
            return category; // 생성된 카테고리 반환
        }).toList(); // 카테고리 리스트로 변환
        return articleService.findByCategories(categories); // 카테고리 리스트로 기사 조회
    }

    // 태그 1개로 기사 조회
    @GetMapping("/tag/{tagCode}")
    public List<Article> getArticlesByTag(@PathVariable Long tagCode) {
        Tag tag = new Tag(); // 태그 객체 생성
        tag.setId(tagCode); // 태그 ID 설정
        return articleService.findByTag(tag); // 태그로 기사 조회
    }

    // 태그 n개로 기사 조회
    @PostMapping("/tags")
    public List<Article> getArticlesByTags(@RequestBody List<Long> tagCodes) {
        // 태그 리스트 생성
        // tagCodes 리스트의 각 code 를 Tag 객체로 변환한 후 태그 리스트에 저장
        List<Tag> tags = tagCodes.stream().map(code -> {
            Tag tag = new Tag(); // 태그 객체 생성
            tag.setId(code); // 태그 ID 설정
            return tag; // 생성된 태그 반환
        }).toList(); // 태그 리스트로 변환
        return articleService.findByTags(tags); // 태그 리스트로 기사 조회
    }

    // 카테고리 1개, 태그 1개로 기사 조회
    @GetMapping("/category/{categoryId}/tag/{tagCode}")
    public List<Article> getArticlesByCategoryAndTag(@PathVariable Long categoryId, @PathVariable Long tagCode) {
        Category category = new Category(); // 카테고리 객체 생성
        category.setId(categoryId); // 카테고리 ID 설정
        Tag tag = new Tag(); // 태그 객체 생성
        tag.setId(tagCode); // 태그 ID 설정
        return articleService.findByCategoryAndTag(category, tag); // 카테고리와 태그로 기사 조회
    }

    // 카테고리 1개, 태그 n개로 기사 조회
    @PostMapping("/category/{categoryId}/tags")
    public List<Article> getArticlesByCategoryAndTags(@PathVariable Long categoryId, @RequestBody List<Long> tagCodes) {
        Category category = new Category(); // 카테고리 객체 생성
        category.setId(categoryId); // 카테고리 ID 설정
        // 태그 리스트 생성
        // tagCodes 리스트의 각 code 를 Tag 객체로 변환한 후 태그 리스트에 저장
        List<Tag> tags = tagCodes.stream().map(code -> {
            Tag tag = new Tag(); // 태그 객체 생성
            tag.setId(code); // 태그 ID 설정
            return tag; // 생성된 태그 반환
        }).toList(); // 태그 리스트로 변환
        return articleService.findByCategoryAndTags(category, tags); // 카테고리와 태그 리스트로 기사 조회
    }

    // 카테고리 n개, 태그 1개로 기사 조회
    @PostMapping("/categories/tag/{tagCode}")
    public List<Article> getArticlesByCategoriesAndTag(@RequestBody List<Long> categoryIds, @PathVariable Long tagCode) {
        // 카테고리 리스트 생성
        // categoryIds 리스트의 각 ID를 Category 객체로 변환한 후 카테고리 리스트에 저장
        List<Category> categories = categoryIds.stream().map(id -> {
            Category category = new Category(); // 카테고리 객체 생성
            category.setId(id); // 카테고리 ID 설정
            return category; // 생성된 카테고리 반환
        }).toList(); // 카테고리 리스트로 변환
        Tag tag = new Tag(); // 태그 객체 생성
        tag.setId(tagCode); // 태그 ID 설정
        return articleService.findByCategoriesAndTag(categories, tag); // 카테고리 리스트와 태그로 기사 조회
    }

    // 카테고리 n개, 태그 n개로 기사 조회
    @PostMapping("/categories/tags")
    public List<Article> getArticlesByCategoriesAndTags(@RequestBody CategoryTagRequest request) {
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
        return articleService.findByCategoriesAndTags(categories, tags); // 카테고리 리스트와 태그 리스트로 기사 조회
    }

    // 카테고리 ID 리스트와 태그 리스트를 전달하기 위한 DTO 클래스
    @Getter @Setter
    public static class CategoryTagRequest {
        private List<Long> categoryIds; // 카테고리 ID 리스트
        private List<Long> tagCodes; // 태그 리스트
    }

    @GetMapping("/ash-test")
    public List<ArticleWithTagsDTO> getTestRecentArticles() {
        Pageable pageable = PageRequest.of(0, 18, Sort.by(Sort.Direction.DESC, "postDate"));
        List<Article> articles = articleService.findAll(pageable).getContent();

        return articles.stream()
                .map(article -> {
                    ArticleWithTagsDTO dto = new ArticleWithTagsDTO();
                    dto.setId(article.getId());
                    dto.setTitle(article.getTitle());
                    dto.setContent(article.getContent());
                    dto.setPostDate(article.getPostDate());
                    dto.setCategory(article.getCategory());
                    dto.setSource(article.getSource());
                    return dto;
                }).collect(Collectors.toList());
    }

    @GetMapping("/all-ash-test")
    public Page<ArticleWithTagsDTO> getTestAllArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Article> articlePage = articleService.findAll(pageable);

        List<Long> articleIds = new ArrayList<>();

        for(Article x : articlePage) {
            articleIds.add(x.getId());
        }


        Map<Long, List<Tag>> articleTagsMap = tagService.findTagsByArticleIds(articleIds);
        Map<Long, List<Image>> articleImageMap = imageService.findImagesByArticleIds(articleIds);


        List<ArticleWithTagsDTO> articleDTOs = articlePage.getContent().stream()
                .map(article -> {
                    ArticleWithTagsDTO dto = new ArticleWithTagsDTO();
                    dto.setId(article.getId());
                    dto.setTitle(article.getTitle());
                    dto.setContent(article.getContent());
                    dto.setPostDate(article.getPostDate());
                    dto.setCategory(article.getCategory());
                    dto.setSource(article.getSource());
//                    dto.setTags(tagService.findByArticle(article));
                    dto.setTags(articleTagsMap.getOrDefault(article.getId(), Collections.emptyList()));
//                    dto.setImgUrls(imageService.findByArticle(article).stream()
//                            .map(Image::getImgUrl)
//                            .collect(Collectors.toList()));

                    dto.setImgUrls(articleImageMap.getOrDefault(article.getId(), Collections.emptyList()).stream()
                            .map(Image::getImgUrl)
                            .collect(Collectors.toList()));

                    return dto;
                }).collect(Collectors.toList());

        return new PageImpl<>(articleDTOs, pageable, articlePage.getTotalElements());
    }

    @GetMapping("/all")
    public Page<ArticleWithTagsDTO> refinedGetAllArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Article> articlePage = articleService.findAll(pageable);

        List<Long> articleIds = new ArrayList<>();

        for(Article x : articlePage) {
            articleIds.add(x.getId());
        }


        Map<Long, List<Tag>> articleTagsMap = tagService.findTagsByArticleIds(articleIds);
        Map<Long, List<Image>> articleImageMap = imageService.findImagesByArticleIds(articleIds);


        List<ArticleWithTagsDTO> articleDTOs = articlePage.getContent().stream()
                .map(article -> {
                    ArticleWithTagsDTO dto = new ArticleWithTagsDTO();
                    dto.setId(article.getId());
                    dto.setTitle(article.getTitle());
                    dto.setContent(article.getContent());
                    dto.setPostDate(article.getPostDate());
                    dto.setCategory(article.getCategory());
                    dto.setSource(article.getSource());
//                    dto.setTags(tagService.findByArticle(article));
                    dto.setTags(articleTagsMap.getOrDefault(article.getId(), Collections.emptyList()));
//                    dto.setImgUrls(imageService.findByArticle(article).stream()
//                            .map(Image::getImgUrl)
//                            .collect(Collectors.toList()));

                    dto.setImgUrls(articleImageMap.getOrDefault(article.getId(), Collections.emptyList()).stream()
                            .map(Image::getImgUrl)
                            .collect(Collectors.toList()));

                    return dto;
                }).collect(Collectors.toList());

        return new PageImpl<>(articleDTOs, pageable, articlePage.getTotalElements());
    }

    // 트라이에 대한 페이지네이션 테스트 메서드
    @GetMapping("/trie")
    @Transactional
    public Page<ArticleWithTagsDTO> getTrieArticlesDTO(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);

        // getTrieArticles 메서드를 통해 기사를 가져옵니다.
        Page<Article> articlePage = articleService.getTrieArticles(pageable);

        List<Long> articleIds = articlePage.getContent().stream()
                .map(Article::getId)
                .collect(Collectors.toList());

        Map<Long, List<Tag>> articleTagsMap = tagService.findTagsByArticleIds(articleIds);
        Map<Long, List<Image>> articleImageMap = imageService.findImagesByArticleIds(articleIds);

        List<ArticleWithTagsDTO> articleDTOs = articlePage.getContent().stream()
                .map(article -> {
                    ArticleWithTagsDTO dto = new ArticleWithTagsDTO();
                    dto.setId(article.getId());
                    dto.setTitle(article.getTitle());
                    dto.setContent(article.getContent());
                    dto.setPostDate(article.getPostDate());
                    dto.setCategory(article.getCategory());
                    dto.setSource(article.getSource());
                    dto.setTags(articleTagsMap.getOrDefault(article.getId(), Collections.emptyList()));
                    dto.setImgUrls(articleImageMap.getOrDefault(article.getId(), Collections.emptyList()).stream()
                            .map(Image::getImgUrl)
                            .collect(Collectors.toList()));
                    return dto;
                }).collect(Collectors.toList());

        return new PageImpl<>(articleDTOs, pageable, articlePage.getTotalElements());
    }

    // 트라이 저장 값 확인 테스트 메서드
    @GetMapping("/trielist")
    public List<ArticleWithTagsDTO> getTrieArticles() {
        // trie에서 모든 기사를 가져옵니다.
        List<Article> allArticles = articleService.getTrieArticle().collectAllArticles(articleService.getTrieArticle().getRoot());

        System.out.println("Total articles fetched: " + allArticles.size());

        // 모든 기사 ID 리스트를 생성합니다.
        List<Long> articleIds = allArticles.stream()
                .map(Article::getId)
                .collect(Collectors.toList());

        System.out.println("Article IDs:");
        articleIds.forEach(System.out::println);

        // querydsl 로 category 에 대한 lazy 문제 해결

        // 태그 및 이미지 정보를 가져옵니다.
        Map<Long, List<Tag>> articleTagsMap = tagService.findTagsByArticleIds(articleIds);
        Map<Long, List<Image>> articleImageMap = imageService.findImagesByArticleIds(articleIds);

        // 각 Article 객체의 연관 엔티티를 초기화합니다.
        for (Article article : allArticles) {
            Hibernate.initialize(article.getCategory()); // 카테고리 초기화
        }

        // DTO로 매핑합니다.
        List<ArticleWithTagsDTO> articleDTOs = allArticles.stream()
                .map(article -> {
                    ArticleWithTagsDTO dto = new ArticleWithTagsDTO();
                    dto.setId(article.getId());
                    dto.setTitle(article.getTitle());
                    dto.setContent(article.getContent());
                    dto.setPostDate(article.getPostDate());
                    dto.setCategory(article.getCategory());
                    dto.setSource(article.getSource());
                    dto.setTags(articleTagsMap.getOrDefault(article.getId(), Collections.emptyList()));
                    dto.setImgUrls(articleImageMap.getOrDefault(article.getId(), Collections.emptyList()).stream()
                            .map(Image::getImgUrl)
                            .collect(Collectors.toList()));
                    return dto;
                }).collect(Collectors.toList());

        return articleDTOs;
    }

    // 검색 메서드
    @Transactional(readOnly = true)
    // 제목으로 기사 검색하는 메서드
    @GetMapping("/search/{keyWords}")
    public Page<ArticleWithTagsDTO> searchArticles(
            @PathVariable String keyWords,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            HttpServletRequest request) {

        // Trie에서 검색 키워드에 맞는 기사 리스트를 가져옴
        List<Article> searchResults = articleService.searchArticle(keyWords);

        // 페이징을 적용하기 위해 Pageable 생성
        Pageable pageable = PageRequest.of(page, size);

        // 검색된 기사 리스트에 대한 ID 리스트 생성
        List<Long> articleIds = searchResults.stream()
                .map(Article::getId)
                .collect(Collectors.toList());

        // 각 기사에 대한 태그 및 이미지 정보 맵 생성
        Map<Long, List<Tag>> articleTagsMap = tagService.findTagsByArticleIds(articleIds);
        Map<Long, List<Image>> articleImageMap = imageService.findImagesByArticleIds(articleIds);

        // ArticleWithTagsDTO 리스트 생성
        List<ArticleWithTagsDTO> articleDTOs = searchResults.stream()
                .map(article -> {
                    ArticleWithTagsDTO dto = new ArticleWithTagsDTO();
                    dto.setId(article.getId());
                    dto.setTitle(article.getTitle());
                    dto.setContent(article.getContent());
                    dto.setPostDate(article.getPostDate());
                    dto.setCategory(article.getCategory());
                    dto.setSource(article.getSource());
                    dto.setTags(articleTagsMap.getOrDefault(article.getId(), Collections.emptyList()));
                    dto.setImgUrls(articleImageMap.getOrDefault(article.getId(), Collections.emptyList()).stream()
                            .map(Image::getImgUrl)
                            .collect(Collectors.toList()));
                    return dto;
                }).collect(Collectors.toList());

        // 검색 결과를 페이징 처리하여 반환
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), articleDTOs.size());
        List<ArticleWithTagsDTO> pagedArticleDTOs = articleDTOs.subList(start, end);

        return new PageImpl<>(pagedArticleDTOs, pageable, articleDTOs.size());
    }


}
