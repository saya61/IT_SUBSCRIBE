package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.ArticleRank;
import com.sw.journal.journalcrawlerpublisher.domain.OurArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleRankRepository extends JpaRepository<ArticleRank, Long> {
    Optional<ArticleRank> findByArticle(OurArticle ourArticle);
    Optional<ArticleRank> findById(Long id);
    List<ArticleRank> findAllByIsActive(Boolean isActive);
}