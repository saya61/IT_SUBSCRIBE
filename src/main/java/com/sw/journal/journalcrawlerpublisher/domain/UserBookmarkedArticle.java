package com.sw.journal.journalcrawlerpublisher.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_bookmarked_article")
@Getter
@Setter
public class UserBookmarkedArticle {
    @EmbeddedId
    private UserBookmarkedArticleId id;

    @ManyToOne
    @MapsId("memberNumber")
    @JoinColumn(name = "member_number")
    private Member member;

    @ManyToOne
    @MapsId("articleNumber")
    @JoinColumn(name = "our_article_number")
    private OurArticle article;
}
