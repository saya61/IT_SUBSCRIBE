package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.domain.TagArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TagArticleRepository extends JpaRepository<TagArticle, Long> {
    List<TagArticle> findByArticle(Article article);


    // SELECT Ta.*
    // FROM TagArticle ta
    // JOIN Tag t ON ta.tag_id = t.id
    // WHERE ta.article_id IN (1,2,3)
    @Query("SELECT ta FROM TagArticle ta " +
            "JOIN FETCH ta.tag " +
            "WHERE ta.article.id IN :articleIds")
    List<TagArticle> findByArticleIds(@Param("articleIds") List<Long> articleIds);
}