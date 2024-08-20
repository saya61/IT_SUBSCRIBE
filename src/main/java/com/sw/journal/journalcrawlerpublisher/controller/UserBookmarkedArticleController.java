package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.domain.UserBookmarkedArticle;
import com.sw.journal.journalcrawlerpublisher.repository.ArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import com.sw.journal.journalcrawlerpublisher.repository.UserBookmarkedArticleRepository;
import com.sw.journal.journalcrawlerpublisher.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmark")
public class UserBookmarkedArticleController {
    private final BookmarkService bookmarkService;

    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;

    private final UserBookmarkedArticleRepository userBookmarkedArticleRepository;

    @GetMapping("/articles")
    public ResponseEntity<List<Article>> addBookmark() {
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

        List<Article> bookmarkedArticles = bookmarkService.findBookmarkedArticleByMember(currentMember);

        return ResponseEntity.ok(bookmarkedArticles);
    }

    @PostMapping("add-bookmark/{articleId}")
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Article article = optionalArticle.get();

        UserBookmarkedArticle userBookmarkedArticle = new UserBookmarkedArticle();
        userBookmarkedArticle.setMember(currentMember);
        userBookmarkedArticle.setArticle(article);
        userBookmarkedArticleRepository.save(userBookmarkedArticle);

        return ResponseEntity.ok("기사가 북마크에 추가되었습니다.");
    }
}
