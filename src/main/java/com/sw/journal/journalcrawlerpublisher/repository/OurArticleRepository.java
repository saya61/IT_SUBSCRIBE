package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.Category;
import com.sw.journal.journalcrawlerpublisher.domain.OurArticle;
import com.sw.journal.journalcrawlerpublisher.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

// wildmantle 2024-07-03
@Repository
public interface OurArticleRepository extends JpaRepository<OurArticle, Long> {
    // 카테고리별 기사 검색
    List<OurArticle> findByCategory(Category category);

    // n개 카테고리로 검색
    @Query("SELECT a FROM OurArticle a " +
            "WHERE a.category IN :categories")
    List<OurArticle> findByCategories(@Param("categories") List<Category> categories);

    // 태그별 기사 검색
    List<OurArticle> findByTag(Tag tag);

    // n개 태그로 검색
    @Query("SELECT a FROM OurArticle a " +
            "JOIN a.tag t " +
            "WHERE t IN :tags GROUP BY a " +
            "HAVING COUNT(DISTINCT t) = :tagCount")
    List<OurArticle> findByTags(@Param("tags") List<Tag> tags, @Param("tagCount") long tagCount);



    //                                                           //
    //     여기 부터는 카테고리와 태그를 둘 다 이용해서 검색하는 로직      //
    //                                                           //


    // 1개 카테고리, 1개 태그로 검색
    @Query("SELECT a FROM OurArticle a " +
            "WHERE a.category = :category AND a.tag = :tag")
    List<OurArticle> findByCategoryAndTag(@Param("category") Category category,
                                          @Param("tag") Tag tag);


    // 1개 카테로기 , n개 태그로 검색
    @Query("SELECT a FROM OurArticle a " +
            "JOIN a.tag t " +
            "WHERE a.category = :category AND t IN :tags GROUP BY a " +
            "HAVING COUNT(DISTINCT t) = :tagCount")
    List<OurArticle> findByCategoryAndTags(@Param("category") Category category,
                                           @Param("tags") List<Tag> tags,
                                           @Param("tagCount") long tagCount);


    // n개 카테고리, 1개 태그로 검색
    @Query("SELECT a FROM OurArticle a " +
            "WHERE a.category IN :categories AND a.tag = :tag"
    )
    List<OurArticle> findByCategoriesAndTag(@Param("categories") List<Category> categories,
                                            @Param("tag") Tag tag,
                                            @Param("categoryCount") long categoryCount);


    // n개 카테고리, n개 태그로 검색
    @Query("SELECT a FROM OurArticle a " +
            "JOIN a.tag t " +
            "WHERE a.category IN :categories AND t IN :tags " +
            "GROUP BY a " +
            "HAVING COUNT(DISTINCT t) = :tagCount")
    List<OurArticle> findByCategoriesAndTags(@Param("categories") List<Category> categories,
                                             @Param("tags") List<Tag> tags,
                                             @Param("tagCount") long tagCount);
}
