package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.CrawlingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// 크롤링 이벤트 테이블과 연동된 JPA
@Repository
public interface CrawlingEventRepository extends JpaRepository<CrawlingEvent, Long> {
    // 처리 여부로 이벤트 테이블 조회
    List<CrawlingEvent> findByIsEventProcessed(boolean isEventProcessed);
}