package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.Article;
import java.util.List;

public interface ArticleRepositoryCustom {
    List<Article> findArticlesWithCategory();
}
