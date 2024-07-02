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
@Table(name = "user_favorite_category")
@Getter
@Setter
public class UserFavoriteCategory {
    @EmbeddedId
    private UserFavoriteCategoryId id;

    @ManyToOne
    @MapsId("memberNumber")
    @JoinColumn(name = "member_number")
    private Member member;

    @ManyToOne
    @MapsId("categoryCode")
    @JoinColumn(name = "category_code")
    private Category category;
}
