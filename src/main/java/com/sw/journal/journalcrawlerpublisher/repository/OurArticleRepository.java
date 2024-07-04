package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.OurArticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OurArticleRepository extends JpaRepository<OurArticle, Long> {}