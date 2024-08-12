package com.sw.journal.journalcrawlerpublisher.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "user_favorite_category")
@Getter
@Setter
public class UserFavoriteCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @MapsId("memberId")
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @MapsId("categoryId")
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}

