package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    // 로그인 인증 시 유저 데이터 조회 (password, role)
    Optional<Member> findByUsername(String username);

    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
