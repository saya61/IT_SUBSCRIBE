package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.UserBookmarkedArticle;
import com.sw.journal.journalcrawlerpublisher.domain.UserBookmarkedArticleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBookmarkedArticleRepository extends JpaRepository<UserBookmarkedArticle, UserBookmarkedArticleId> {}