package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.ArticleRank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRankRepository extends JpaRepository<ArticleRank, Integer> {
}
