package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByUserNickname(String userNickname);

    // 유저 검색 메서드
    boolean existsByUserNickname(String userNickname);
    boolean existsByUserEmail(String userEmail);
    boolean existsByUserId(String userId);
}
