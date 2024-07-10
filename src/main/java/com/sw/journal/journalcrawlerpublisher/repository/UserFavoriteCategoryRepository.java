package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.domain.UserFavoriteCategory;
import com.sw.journal.journalcrawlerpublisher.domain.UserFavoriteCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFavoriteCategoryRepository extends JpaRepository<UserFavoriteCategory, UserFavoriteCategoryId> {
    // 입력된 회원의 선호 카테고리를 검색
    List<UserFavoriteCategory> findByMember(Member member);
    // 입력된 회원의 선호 카테고리를 삭제
    void deleteByMember(Member member);
}
