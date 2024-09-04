package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.domain.*;
import com.sw.journal.journalcrawlerpublisher.dto.ArticleWithTagsDTO;
import com.sw.journal.journalcrawlerpublisher.repository.ArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import com.sw.journal.journalcrawlerpublisher.repository.UserBookmarkedArticleRepository;
import com.sw.journal.journalcrawlerpublisher.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmark")
public class UserBookmarkedArticleController {
    private final BookmarkService bookmarkService;
    private final ArticleService articleService;
    private final TagService tagService;
    private final ImageService imageService;
    private final CommentService commentService;
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;

    private final UserBookmarkedArticleRepository userBookmarkedArticleRepository;

    @GetMapping("/articles")
    public ResponseEntity<Page<ArticleWithTagsDTO>> getAllBookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        // 현재 로그인한 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 로그인한 사용자의 id를 가져옴
        String currentUsername = authentication.getName();
        // 사용자 id로 Member 객체 조회
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 사용자가 존재하지 않는 경우 401 Unauthorized 응답 반환
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        // 조회된 Member 객체에서 정보 추출
        Member currentMember = member.get();

        Pageable pageable = PageRequest.of(page, size);

        Page<Article> articlePage = bookmarkService.findByMember(currentMember, pageable);

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
                    dto.setTags(articleTagsMap.getOrDefault(article.getId(), Collections.emptyList()));
                    dto.setImgUrls(articleImageMap.getOrDefault(article.getId(), Collections.emptyList()).stream()
                            .map(Image::getImgUrl)
                            .collect(Collectors.toList()));

                    return dto;
                }).collect(Collectors.toList());

        return ResponseEntity.ok(new PageImpl<>(articleDTOs, pageable, articlePage.getTotalElements()));
    }

    @GetMapping("/articles/{articleId}")
    public ResponseEntity<Article> getBookmark(@PathVariable Long articleId) {
        // 현재 로그인한 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 로그인한 사용자의 id를 가져옴
        String currentUsername = authentication.getName();
        // 사용자 id로 Member 객체 조회
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 사용자가 존재하지 않는 경우 204 No Content 응답 반환
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
        // 조회된 Member 객체에서 정보 추출
        Member currentMember = member.get();

        // 선택한 기사 가져오기
        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        if (optionalArticle.isEmpty()) {
            // 기사가 없으면 404 Not Found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        Article article = optionalArticle.get();

        try {
            Optional<UserBookmarkedArticle> bookmarkedArticle = bookmarkService.findBookmarkedArticlesByMemberAndArticle(currentMember, article);

            if(bookmarkedArticle.isPresent()){
                return ResponseEntity.ok(bookmarkedArticle.get().getArticle());
            }
            else {
                // 사용자가 북마크한 기사가 없으면 204 No Content
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            }
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/add-bookmark/{articleId}")
    public ResponseEntity<String> addBookmark(@PathVariable Long articleId) {
        // 현재 로그인한 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 로그인한 사용자의 id를 가져옴
        String currentUsername = authentication.getName();
        // 사용자 id로 Member 객체 조회
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 사용자가 존재하지 않는 경우 401 Unauthorized 응답 반환
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        // 조회된 Member 객체에서 정보 추출
        Member currentMember = member.get();

        // 선택한 기사 가져오기
        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        if (optionalArticle.isEmpty()) {
            // 기사가 없으면 404 Not Found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        Article article = optionalArticle.get();

        Optional<UserBookmarkedArticle> bookmarkedArticle = bookmarkService.findBookmarkedArticlesByMemberAndArticle(currentMember, article);

        if(bookmarkedArticle.isEmpty()){
            UserBookmarkedArticle userBookmarkedArticle = new UserBookmarkedArticle();
            userBookmarkedArticle.setMember(currentMember);
            userBookmarkedArticle.setArticle(article);
            userBookmarkedArticleRepository.save(userBookmarkedArticle);

            return ResponseEntity.ok("기사가 북마크에 추가되었습니다.");
        }
        else {
            return ResponseEntity.ok("기사가 이미 추가되었습니다.");
        }
    }

    @DeleteMapping("/delete-bookmark/{articleId}")
    public ResponseEntity<String> deleteBookmark(@PathVariable Long articleId) {
        // 현재 로그인한 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 로그인한 사용자의 id를 가져옴
        String currentUsername = authentication.getName();
        // 사용자 id로 Member 객체 조회
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 사용자가 존재하지 않는 경우 401 Unauthorized 응답 반환
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        // 조회된 Member 객체에서 정보 추출
        Member currentMember = member.get();

        // 선택한 기사 가져오기
        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        if (optionalArticle.isEmpty()) {
            // 기사가 없으면 404 Not Found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        Article article = optionalArticle.get();

        Optional<UserBookmarkedArticle> userBookmarkedArticle = userBookmarkedArticleRepository.findBookmarkedArticlesByMemberAndArticle(currentMember, article);
        if(userBookmarkedArticle.isPresent()){
            userBookmarkedArticleRepository.delete(userBookmarkedArticle.get());
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }


        return ResponseEntity.ok("기사가 북마크에서 삭제되었습니다.");
    }
}
