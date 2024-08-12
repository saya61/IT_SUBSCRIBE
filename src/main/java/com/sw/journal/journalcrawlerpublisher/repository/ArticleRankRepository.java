package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.domain.ArticleRank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleRankRepository extends JpaRepository<ArticleRank, Long> {
    Optional<ArticleRank> findByArticle(Article article);
    Optional<ArticleRank> findById(Long id);
    List<ArticleRank> findAllByIsActive(Boolean isActive);
}