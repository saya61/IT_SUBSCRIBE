package com.sw.journal.journalcrawlerpublisher.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_bookmarked_article")
@Getter
@Setter
public class UserBookmarkedArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @MapsId("memberId")
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @MapsId("articleId")
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;
}
