package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.domain.OurArticle;
import com.sw.journal.journalcrawlerpublisher.domain.Image;
import com.sw.journal.journalcrawlerpublisher.domain.UserFavoriteCategory;
import com.sw.journal.journalcrawlerpublisher.dto.OurArticleWithTagsDTO;
import com.sw.journal.journalcrawlerpublisher.repository.UserFavoriteCategoryRepository;
import com.sw.journal.journalcrawlerpublisher.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController //
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

//    // 사용자 선호 카테고리로 기사 조회
//    @GetMapping("/user/articles/favorites-category")
//    public ResponseEntity<List<OurArticleWithTagsDTO>> getArticlesByUserFavoriteCategories() {
//        // 현재 로그인한 사용자 정보를 가져옴
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        // 현재 로그인한 사용자의 id를 가져옴
//        String currentUsername = authentication.getName();
//        // 사용자 id로 Member 객체 조회
//        Optional<Member> member = memberService.findByUsername(currentUsername);
//
//        // 사용자가 존재하지 않는 경우 401 Unauthorized 응답 반환
//        if (!member.isPresent()) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//
//        // 조회된 Member 객체에서 정보 추출
//        Member foundMember = member.get();
//        // 사용자 선호 카테고리로 기사 조회
//        List<OurArticle> articles = recommendArticleService.findByUserFavoriteCategories(foundMember);
//        // 기사 리스트를 DTO 리스트로 변환
//        List<OurArticleWithTagsDTO> articleDTOs = articles.stream()
//                .map(article -> {
//                    // DTO 객체 생성
//                    OurArticleWithTagsDTO dto = new OurArticleWithTagsDTO();
//                    // 기사 정보 설정
//                    dto.setId(article.getId());
//                    dto.setTitle(article.getTitle());
//                    dto.setContent(article.getContent());
//                    dto.setPostDate(article.getPostDate());
//                    dto.setCategory(article.getCategory());
//                    dto.setSource(article.getSource()); // 기사 출처 설정
//                    dto.setTags(tagService.findByArticle(article)); // 기사 태그 설정
//                    dto.setImgUrls(imageService.findByArticle(article).stream()
//                            .map(Image::getImgUrl)
//                            .collect(Collectors.toList())); // 기사 이미지 URL 설정
//                    return dto; // DTO 반환
//                }).collect(Collectors.toList()); // 모든 DTO를 리스트로 수집
//
//        return ResponseEntity.ok(articleDTOs); // DTO 리스트를 응답으로 반환
//    }

    // 유저 선호 카테고리 기반으로 최근 12개 기사 가져오기
    @GetMapping("/recent")
    public ResponseEntity<List<OurArticleWithTagsDTO>> getArticlesByUserFavoriteCategories() {
        // 현재 로그인한 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 로그인한 사용자의 id를 가져옴
        String currentUsername = authentication.getName();
        // 사용자 id로 Member 객체 조회
        Optional<Member> member = memberService.findByUsername(currentUsername);

        // 사용자가 존재하지 않는 경우 401 Unauthorized 응답 반환
        if (!member.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 조회된 Member 객체에서 정보 추출
        Member foundMember = member.get();
        // 기사 리스트 변수
        List<OurArticle> articles;

        // 유저 선호 카테고리 조회
        List<UserFavoriteCategory> favoriteCategories = userFavoriteCategoryRepository.findByMember(foundMember);
        // 선호 카테고리가 없을 경우
        if (favoriteCategories.isEmpty()) {
            // 최근 기사 12개 조회
            articles = recommendArticleService.findRecentArticles(12);
        } else { // 선호 카테고리가 있으면 해당 카테고리의 기사 조회
            articles = recommendArticleService.findByUserFavoriteCategories(foundMember);
        }

        // 기사 리스트를 DTO 리스트로 변환
        List<OurArticleWithTagsDTO> articleDTOs = articles.stream()
                .map(article -> {
                    OurArticleWithTagsDTO dto = new OurArticleWithTagsDTO(); // DTO 객체 생성
                    // 기사 정보 설정
                    dto.setId(article.getId());
                    dto.setTitle(article.getTitle());
                    dto.setContent(article.getContent());
                    dto.setPostDate(article.getPostDate());
                    dto.setCategory(article.getCategory());
                    dto.setSource(article.getSource()); // 기사 출처 설정
                    dto.setTags(tagService.findByArticle(article)); // 기사 태그 설정
                    dto.setImgUrls(imageService.findByArticle(article).stream()
                            .map(Image::getImgUrl)
                            .collect(Collectors.toList())); // 기사 이미지 URL 설정
                    return dto; // DTO 반환
                }).collect(Collectors.toList()); // 모든 DTO를 리스트로 수집

        return ResponseEntity.ok(articleDTOs); // DTO 리스트를 응답으로 반환
    }
}
