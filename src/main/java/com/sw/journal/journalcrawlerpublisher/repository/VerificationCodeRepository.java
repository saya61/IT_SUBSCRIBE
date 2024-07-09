package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByEmail(String email);
    void deleteByEmail(String email);
}
