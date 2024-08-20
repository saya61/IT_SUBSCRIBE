package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.Comment;
import com.sw.journal.journalcrawlerpublisher.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByArticle(Article article);

    List<Comment> findByArticleAndParentCommentIsNull(Article article); // 최상위 댓글 조회
    List<Comment> findByParentCommentOrderByCreatedAtDesc(Comment parentComment); // 대댓글 최신순 조회
    int countByParentComment(Comment parentComment); // 대댓글 수 계산


}
