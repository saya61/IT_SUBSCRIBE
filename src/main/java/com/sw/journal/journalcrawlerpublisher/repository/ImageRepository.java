package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.Image;
import com.sw.journal.journalcrawlerpublisher.domain.OurArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByOurArticle(OurArticle ourArticle);
    @Query("SELECT i FROM Image i where i.ourArticle.id IN :articleIds")
    List<Image> findByArticleIds(@Param("articleIds") List<Long> articleIds);
}
