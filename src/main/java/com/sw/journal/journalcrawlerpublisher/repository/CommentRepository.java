package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByArticle(Article article);
}
