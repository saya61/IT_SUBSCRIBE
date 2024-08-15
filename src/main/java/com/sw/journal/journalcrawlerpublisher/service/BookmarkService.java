package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.domain.UserBookmarkedArticle;
import com.sw.journal.journalcrawlerpublisher.repository.ArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import com.sw.journal.journalcrawlerpublisher.repository.UserBookmarkedArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final UserBookmarkedArticleRepository userBookmarkedArticleRepository;

    // 사용자에 연결된 기사 조회
    public List<Article> findBookmarkedArticleByMember(Member member) {
        // 사용자에 연결된 UserBookmarkedArticle 리스트 조회
        List<UserBookmarkedArticle> userBookmarkedArticles = userBookmarkedArticleRepository.findBookmarkedArticlesByMember(member);

        return userBookmarkedArticles.stream()
                .map(UserBookmarkedArticle::getArticle)
                .toList();
    }

    public void saveBookmarkedArticle(Article article) {
        UserBookmarkedArticle userBookmarkedArticle = new UserBookmarkedArticle();
        userBookmarkedArticle.setArticle(article);
        userBookmarkedArticleRepository.save(userBookmarkedArticle);
    }
}
