package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.domain.Category;
import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.domain.UserBookmarkedArticle;
import com.sw.journal.journalcrawlerpublisher.repository.ArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import com.sw.journal.journalcrawlerpublisher.repository.UserBookmarkedArticleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final UserBookmarkedArticleRepository userBookmarkedArticleRepository;

    // 사용자에 연결된 기사 조회
    public List<Long> findBookmarkedArticleByMember(Member member) {
        // 사용자에 연결된 UserBookmarkedArticle 리스트 조회
        List<UserBookmarkedArticle> userBookmarkedArticles = userBookmarkedArticleRepository.findBookmarkedArticlesByMember(member);
        List<Article> articles = userBookmarkedArticles.stream()
                .map(UserBookmarkedArticle::getArticle)
                .toList();
        return articles.stream()
                .map(Article::getId)
                .toList();
    }

    public Optional<UserBookmarkedArticle> findBookmarkedArticlesByMemberAndArticle(Member member, Article article) {
        return userBookmarkedArticleRepository.findBookmarkedArticlesByMemberAndArticle(member, article);
    }

    // 카테고리별 페이지네이션된 기사 검색
    public Page<Article> findByMember(Member member, Pageable pageable) {
        Page<UserBookmarkedArticle> bookmarkedArticlePage =  userBookmarkedArticleRepository.findByMember(member, pageable);
        return bookmarkedArticlePage.map(UserBookmarkedArticle::getArticle);
    }

    public List<UserBookmarkedArticle> getAllBookmarkedArticles() {
        return userBookmarkedArticleRepository.findAll();
    }
    public DataModel getMahoutDataModel() {
        // 데이터베이스에서 데이터 로드
        List<UserBookmarkedArticle> bookmarks = getAllBookmarkedArticles();

        // 데이터 모델 생성
        FastByIDMap<PreferenceArray> preferences = new FastByIDMap<>();
        for (UserBookmarkedArticle bookmark : bookmarks) {
            long userId = bookmark.getMember().getId();
            long articleId = bookmark.getArticle().getId();
            double rating = 10.0;

            // 사용자별로 데이터를 묶음
            PreferenceArray userPreferences = preferences.get(userId);
            if (userPreferences == null) {
                userPreferences = new GenericUserPreferenceArray(1);
                userPreferences.setUserID(0, userId);
            }
            userPreferences.setItemID(0, articleId);
            userPreferences.setValue(0, (float) rating);
            preferences.put(userId, userPreferences);
        }

        return new GenericDataModel(preferences);
    }




//    public void saveBookmarkedArticle(Article article) {
//        UserBookmarkedArticle userBookmarkedArticle = new UserBookmarkedArticle();
//        userBookmarkedArticle.setArticle(article);
//        userBookmarkedArticleRepository.save(userBookmarkedArticle);
//    }
}
