package com.sw.journal.journalcrawlerpublisher.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Entity
@Table(name = "user_bookmarked_article")
@Getter
@Setter
public class UserBookmarkedArticle {
    @EmbeddedId
    private UserBookmarkedArticleId id;

    @ManyToOne
    @MapsId("memberId")
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @MapsId("articleId")
    @JoinColumn(name = "article_id", nullable = false)
    private OurArticle article;
}
