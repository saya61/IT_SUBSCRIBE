package com.sw.journal.journalcrawlerpublisher.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "article_rank")
@Getter
@Setter
public class ArticleRank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false, unique = true)
    private OurArticle article;

    @Column(nullable = false)
    private Long count;

    @Column(nullable = false)
    private Boolean isActive;
}
