package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.constant.CommentReportStatus;
import com.sw.journal.journalcrawlerpublisher.domain.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByStatus(CommentReportStatus reportStatus, Pageable pageable);
}
