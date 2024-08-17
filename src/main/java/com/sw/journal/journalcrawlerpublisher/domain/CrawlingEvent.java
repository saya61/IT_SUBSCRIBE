package com.sw.journal.journalcrawlerpublisher.domain;

import com.sw.journal.journalcrawlerpublisher.constant.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

// 크롤링 이벤트를 저장하는 DB
@Entity
@Table(name = "crawling_event")
@Getter
@Setter
public class CrawlingEvent {
    // 기본키
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이벤트 발생 일자
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 신작 기사 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // 신작 기사
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    // 이벤트 처리 여부
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean eventProcessed;

    @Override
    public String toString() {
        return "CrawlingEvent{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", category='" + category + '\'' +
                ", eventProcessed=" + eventProcessed +
                '}';
    }
}
