package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.UserFavoriteCategory;
import com.sw.journal.journalcrawlerpublisher.domain.UserFavoriteCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFavoriteCategoryRepository extends JpaRepository<UserFavoriteCategory, UserFavoriteCategoryId> {
    List<UserFavoriteCategory> findByIdMemberId(Long memberId);
}
