package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.OurArticle;
import com.sw.journal.journalcrawlerpublisher.domain.Tag;
import com.sw.journal.journalcrawlerpublisher.domain.TagArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagArticleRepository extends JpaRepository<TagArticle, Long> {
    List<TagArticle> findByArticle(OurArticle article);
}