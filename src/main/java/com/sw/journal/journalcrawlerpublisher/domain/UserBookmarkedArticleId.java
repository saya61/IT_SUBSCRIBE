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
// UserBookmarkedArticle 복합키(사용자 id, 기사 id) 클래스
public class UserBookmarkedArticleId implements Serializable {
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "article_id")
    private Long articleId;

    // 두 객체를 비교하여 동일한지 판단하는 equals 메서드 재정의
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // 자기 자신과 비교하면 항상 true
        if (o == null || getClass() != o.getClass()) return false; // null 이거나 다른 클래스 타입이면 false
        UserBookmarkedArticleId that = (UserBookmarkedArticleId) o; // 비교할 대상 객체를 동일한 타입으로 캐스팅
        return Objects.equals(memberId, that.memberId) && // memberId 필드 값 비교
                Objects.equals(articleId, that.articleId); // articleId 필드 값 비교
    }

    // 해시코드 생성 메서드 재정의
    // 객체를 해시맵 등에서 사용할 수 있도록 해줌
    @Override
    public int hashCode() {
        return Objects.hash(memberId, articleId);
    } // memberId와 articleId 필드를 사용해 해시코드 생성
}
