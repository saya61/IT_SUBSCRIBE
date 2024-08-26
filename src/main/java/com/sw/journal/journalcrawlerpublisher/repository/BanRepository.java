package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.Ban;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BanRepository extends JpaRepository<Ban, Long> {

    List<Ban> findByMemberIdAndActiveTrue(Long memberId);
    Optional<Ban> findTopByMemberIdAndActiveTrueOrderByBanEndDateDesc(Long memberId);

    List<Ban> findByActiveTrueAndBanEndDateBefore(LocalDate now);

    Optional<Ban> findFirstByMemberIdAndActiveTrueOrderByBanStartDateDesc(Long memberId);

    void deleteByReportId(Long reportId);
}
