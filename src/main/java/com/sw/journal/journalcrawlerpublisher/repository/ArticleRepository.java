package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.Category;
import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.domain.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository
        extends JpaRepository<Article, Long> ,ArticleRepositoryCustom {

    // 카테고리별 페이지네이션된 기사 검색
    Page<Article> findByCategory(Category category, Pageable pageable);

    // 기사 URL로 기사 검색
    Optional<Article> findBySource(String source);

    // 카테고리별 기사 검색
    List<Article> findByCategory(Category category);

    // n개 카테고리로 검색
    @Query("SELECT a FROM Article a WHERE a.category IN :categories")
    List<Article> findByCategories(@Param("categories") List<Category> categories);

    // n개 카테고리 최신순 검색
    @Query("SELECT a FROM Article a WHERE a.category IN :categories ORDER BY a.postDate DESC")
    List<Article> findTopByCategoriesOrderByPostDate(@Param("categories") List<Category> categories, Pageable pageable);

    // 태그별 기사 검색
    @Query("SELECT ta.article FROM TagArticle ta WHERE ta.tag = :tag")
    List<Article> findByTag(@Param("tag") Tag tag);

    // n개 태그로 검색
    @Query("SELECT ta.article " +
            "FROM TagArticle ta " +
            "WHERE ta.tag IN :tags " +
            "GROUP BY ta.article " +
            "HAVING COUNT(DISTINCT ta.tag) = :tagCount")
    List<Article> findByTags(@Param("tags") List<Tag> tags, @Param("tagCount") long tagCount);

    // 1개 카테고리, 1개 태그로 검색
    @Query("SELECT a " +
            "FROM Article a " +
            "JOIN TagArticle ta " +
            "ON a.id = ta.article.id " +
            "WHERE a.category = :category " +
            "AND ta.tag = :tag")
    List<Article> findByCategoryAndTag(@Param("category") Category category, @Param("tag") Tag tag);

    // 1개 카테고리, n개 태그로 검색
    @Query("SELECT a " +
            "FROM Article a " +
            "JOIN TagArticle ta " +
            "ON a.id = ta.article.id " +
            "WHERE a.category = :category " +
            "AND ta.tag IN :tags " +
            "GROUP BY a " +
            "HAVING COUNT(DISTINCT ta.tag) = :tagCount")
    List<Article> findByCategoryAndTags(@Param("category") Category category, @Param("tags") List<Tag> tags, @Param("tagCount") long tagCount);

    // n개 카테고리, 1개 태그로 검색
    @Query("SELECT a " +
            "FROM Article a " +
            "JOIN TagArticle ta " +
            "ON a.id = ta.article.id " +
            "WHERE a.category IN :categories " +
            "AND ta.tag = :tag")
    List<Article> findByCategoriesAndTag(@Param("categories") List<Category> categories, @Param("tag") Tag tag);

    // n개 카테고리, n개 태그로 검색
    @Query("SELECT a " +
            "FROM Article a " +
            "JOIN TagArticle ta " +
            "ON a.id = ta.article.id " +
            "WHERE a.category IN :categories " +
            "AND ta.tag IN :tags " +
            "GROUP BY a " +
            "HAVING COUNT(DISTINCT ta.tag) = :tagCount")
    List<Article> findByCategoriesAndTags(@Param("categories") List<Category> categories, @Param("tags") List<Tag> tags, @Param("tagCount") long tagCount);
}
