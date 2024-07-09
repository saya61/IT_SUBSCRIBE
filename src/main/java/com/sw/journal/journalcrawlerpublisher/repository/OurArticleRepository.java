package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.Category;
import com.sw.journal.journalcrawlerpublisher.domain.OurArticle;
import com.sw.journal.journalcrawlerpublisher.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// wild-mantle 07-08
@Repository
public interface OurArticleRepository extends JpaRepository<OurArticle, Long> {

    // 카테고리별 기사 검색
    List<OurArticle> findByCategory(Category category);

    // n개 카테고리로 검색
    @Query("SELECT a FROM OurArticle a WHERE a.category IN :categories")
    List<OurArticle> findByCategories(@Param("categories") List<Category> categories);

    // 태그별 기사 검색
    @Query("SELECT ta.article FROM TagArticle ta WHERE ta.tag = :tag")
    List<OurArticle> findByTag(@Param("tag") Tag tag);

    // n개 태그로 검색
    @Query("SELECT ta.article " +
            "FROM TagArticle ta " +
            "WHERE ta.tag IN :tags " +
            "GROUP BY ta.article " +
            "HAVING COUNT(DISTINCT ta.tag) = :tagCount")
    List<OurArticle> findByTags(@Param("tags") List<Tag> tags, @Param("tagCount") long tagCount);

    // 1개 카테고리, 1개 태그로 검색
    @Query("SELECT a " +
            "FROM OurArticle a " +
            "JOIN TagArticle ta " +
            "ON a.id = ta.article.id " +
            "WHERE a.category = :category " +
            "AND ta.tag = :tag")
    List<OurArticle> findByCategoryAndTag(@Param("category") Category category, @Param("tag") Tag tag);

    // 1개 카테고리, n개 태그로 검색
    @Query("SELECT a " +
            "FROM OurArticle a " +
            "JOIN TagArticle ta " +
            "ON a.id = ta.article.id " +
            "WHERE a.category = :category " +
            "AND ta.tag IN :tags " +
            "GROUP BY a " +
            "HAVING COUNT(DISTINCT ta.tag) = :tagCount")
    List<OurArticle> findByCategoryAndTags(@Param("category") Category category, @Param("tags") List<Tag> tags, @Param("tagCount") long tagCount);

    // n개 카테고리, 1개 태그로 검색
    @Query("SELECT a " +
            "FROM OurArticle a " +
            "JOIN TagArticle ta " +
            "ON a.id = ta.article.id " +
            "WHERE a.category IN :categories " +
            "AND ta.tag = :tag")
    List<OurArticle> findByCategoriesAndTag(@Param("categories") List<Category> categories, @Param("tag") Tag tag);

    // n개 카테고리, n개 태그로 검색
    @Query("SELECT a " +
            "FROM OurArticle a " +
            "JOIN TagArticle ta " +
            "ON a.id = ta.article.id " +
            "WHERE a.category IN :categories " +
            "AND ta.tag IN :tags " +
            "GROUP BY a " +
            "HAVING COUNT(DISTINCT ta.tag) = :tagCount")
    List<OurArticle> findByCategoriesAndTags(@Param("categories") List<Category> categories, @Param("tags") List<Tag> tags, @Param("tagCount") long tagCount);
}
