package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.domain.UserBookmarkedArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserBookmarkedArticleRepository extends JpaRepository<UserBookmarkedArticle, Long> {

    @Query("SELECT ub FROM UserBookmarkedArticle ub " +
    "JOIN FETCH ub.member JOIN FETCH ub.article " +
    "WHERE ub.member = :member")
    List<UserBookmarkedArticle> findBookmarkedArticlesByMember(@Param("member") Member member);

    @Query("SELECT ub FROM UserBookmarkedArticle ub " +
            "JOIN FETCH ub.member JOIN FETCH ub.article " +
            "WHERE ub.member = :member AND ub.article = :article")
    Optional<UserBookmarkedArticle> findBookmarkedArticlesByMemberAndArticle(@Param("member") Member member, @Param("article") Article article);

    Page<UserBookmarkedArticle> findByMember(Member member, Pageable pageable);
}