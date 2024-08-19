package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.Image;
import com.sw.journal.journalcrawlerpublisher.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByArticle(Article article);
    @Query("SELECT i FROM Image i where i.article.id IN :articleIds")
    List<Image> findByArticleIds(@Param("articleIds") List<Long> articleIds);

    void deleteByArticleId(Long articleId);
}
