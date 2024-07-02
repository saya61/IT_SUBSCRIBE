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

@Entity
@Table(name = "article_rank")
@Getter
@Setter
public class ArticleRank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_rank_id")
    private Integer articleRankId;

    @ManyToOne
    @JoinColumn(name = "our_article_number", nullable = false)
    private OurArticle article;

    @Column(name = "article_count", nullable = false)
    private Integer articleCount;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
