package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.TagArticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagArticleRepository extends JpaRepository<TagArticle, Long> {}