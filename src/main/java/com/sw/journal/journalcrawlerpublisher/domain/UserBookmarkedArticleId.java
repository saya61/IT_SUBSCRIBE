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
public class UserBookmarkedArticleId implements Serializable {
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "article_id")
    private Long articleId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserBookmarkedArticleId that = (UserBookmarkedArticleId) o;
        return Objects.equals(memberId, that.memberId) && Objects.equals(articleId, that.articleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, articleId);
    }
}
