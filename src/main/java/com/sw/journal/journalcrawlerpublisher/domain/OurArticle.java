package com.sw.journal.journalcrawlerpublisher.domain;

import jakarta.persistence.*;
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
    private Long id;

    @Column(nullable = false)
    private String title;

    //@Lob    // 큰 문자열을 저장하기 위한 어노테이션
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime postDate;

    @ManyToOne  //@OneToOne 이 맞는거 같다
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(unique = true, nullable = false)
    private String source;

    @Override
    public String toString() {
        return "OurArticle{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", postDate=" + postDate +
                ", category=" + category +
                ", source='" + source + '\'' +
                '}';
    }
}
