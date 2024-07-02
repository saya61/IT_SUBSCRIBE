package com.sw.journal.journalcrawlerpublisher.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Getter
@Setter
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_no")
    private Integer reportNo;

    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment reportedComment;

    @ManyToOne
    @JoinColumn(name = "member_number", nullable = false)
    private Member reportingMember;

    @Column(name = "report_reason", nullable = false)
    private String reportReason;

    @Column(name = "report_date", nullable = false)
    private LocalDateTime reportDate;
}
