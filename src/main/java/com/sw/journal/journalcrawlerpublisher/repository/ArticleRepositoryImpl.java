package com.sw.journal.journalcrawlerpublisher.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.domain.QArticle;
import com.sw.journal.journalcrawlerpublisher.domain.QCategory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    // Bean 생성을위한 QuerydslConfig 생성 필요
    private final JPAQueryFactory queryFactory;

    public ArticleRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<Article> findArticlesWithCategory() {
        QArticle article = QArticle.article;
        QCategory category = QCategory.category;

        return queryFactory.selectFrom(article)
                .join(article.category, category).fetchJoin()
                .fetch();
    }
}
