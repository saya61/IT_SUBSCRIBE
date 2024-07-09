package com.sw.journal.journalcrawlerpublisher.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
public class UserFavoriteCategoryId implements Serializable {
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "category_id")
    private Long categoryId;

    public UserFavoriteCategoryId() {}

    public UserFavoriteCategoryId(Long memberId, Long categoryId) {
        this.memberId = memberId;
        this.categoryId = categoryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserFavoriteCategoryId that = (UserFavoriteCategoryId) o;
        return Objects.equals(memberId, that.memberId) && Objects.equals(categoryId, that.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, categoryId);
    }
}
