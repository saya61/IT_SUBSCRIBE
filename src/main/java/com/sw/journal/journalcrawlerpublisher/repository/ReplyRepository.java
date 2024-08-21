package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.Reply;
import com.sw.journal.journalcrawlerpublisher.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    List<Reply> findByParentCommentOrderByCreatedAtDesc(Comment parentComment); // 대댓글 최신순 조회
    int countByParentComment(Comment parentComment); // 대댓글 수 계산

}
