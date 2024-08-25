package com.sw.journal.journalcrawlerpublisher.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "ban")
@Getter
@Setter
public class Ban {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 제재를 받은 사용자

    @ManyToOne
    @JoinColumn(name = "report_id", nullable = false)
    private Report report; // 제재와 관련된 신고

    @Column(nullable = false)
    private LocalDate banStartDate; // 제재 시작일

    @Column(nullable = false)
    private LocalDate  banEndDate; // 제재 종료일

    @Column(nullable = false)
    private String reason; // 제재 사유

    @Column(nullable = false)
    private boolean active; // 제재 활성화 상태
}
