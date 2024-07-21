package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.domain.OurArticle;
import com.sw.journal.journalcrawlerpublisher.domain.Image;
import com.sw.journal.journalcrawlerpublisher.domain.UserFavoriteCategory;
import com.sw.journal.journalcrawlerpublisher.dto.OurArticleWithTagsDTO;
import com.sw.journal.journalcrawlerpublisher.repository.UserFavoriteCategoryRepository;
import com.sw.journal.journalcrawlerpublisher.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/recommend-article")
public class RecommendedArticleController {

    private final RecommendArticleService recommendArticleService;
    private final MemberService memberService;
    private final TagService tagService;
    private final ImageService imageService;
    private final UserFavoriteCategoryRepository userFavoriteCategoryRepository;

    @Autowired
    public RecommendedArticleController(RecommendArticleService recommendArticleService, MemberService memberService, TagService tagService, ImageService imageService, UserFavoriteCategoryRepository userFavoriteCategoryRepository) {
        this.recommendArticleService = recommendArticleService;
        this.memberService = memberService;
        this.tagService = tagService;
        this.imageService = imageService;
        this.userFavoriteCategoryRepository = userFavoriteCategoryRepository;
    }


//    // 유저 선호 카테고리 기사 검색
//    @GetMapping("/user/articles/favorites-category")
//    public ResponseEntity<List<OurArticleWithTagsDTO>> getArticlesByUserFavoriteCategories() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String currentUsername = authentication.getName();
//        Optional<Member> member = memberService.findByUsername(currentUsername);
//
//        if (!member.isPresent()) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//
//        Member foundMember = member.get();
//        List<OurArticle> articles = recommendArticleService.findByUserFavoriteCategories(foundMember);
//
//        List<OurArticleWithTagsDTO> articleDTOs = articles.stream()
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
//        return ResponseEntity.ok(articleDTOs);
//    }

    // 유저 선호 카테고리 기반 최근 12개 기사 가져오기
    @GetMapping("/recent")
    public ResponseEntity<List<OurArticleWithTagsDTO>> getArticlesByUserFavoriteCategories() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<Member> member = memberService.findByUsername(currentUsername);

        if (!member.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Member foundMember = member.get();
        List<OurArticle> articles;

        // 유저 선호 카테고리가 있는지 확인
        List<UserFavoriteCategory> favoriteCategories = userFavoriteCategoryRepository.findByMember(foundMember);
        if (favoriteCategories.isEmpty()) {
            // 선호 카테고리가 없을 경우 최근 기사 반환
            articles = recommendArticleService.findRecentArticles(12);
        } else {
            // 선호 카테고리가 있을 경우 해당 카테고리의 기사 반환
            articles = recommendArticleService.findByUserFavoriteCategories(foundMember);
        }

        List<OurArticleWithTagsDTO> articleDTOs = articles.stream()
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

        return ResponseEntity.ok(articleDTOs);
    }
}
