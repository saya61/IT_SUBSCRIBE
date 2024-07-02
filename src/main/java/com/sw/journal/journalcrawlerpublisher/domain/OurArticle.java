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
@Table(name = "our_article")
@Getter
@Setter
public class OurArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "our_article_number")
    private Integer articleNumber;

    @Column(name = "our_article_title", nullable = false, length = 50)
    private String articleTitle;

    @Column(name = "our_article_content", nullable = false)
    private String articleContent;

    @Column(name = "our_article_image_link", length = 255)
    private String articleImageLink;

    @ManyToOne
    @JoinColumn(name = "category_code")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "tag_code")
    private Tag tag;

    @Column(name = "hashed_url", length = 255)
    private String hashedUrl;

    @Column(name = "our_publication_date")
    private LocalDateTime ourPublicationDate;
}
