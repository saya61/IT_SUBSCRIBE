package com.sw.journal.journalcrawlerpublisher.domain;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class UserBookmarkedArticleId implements Serializable {
    private Integer memberNumber;
    private Integer articleNumber;

    // Default constructor
    public UserBookmarkedArticleId() {}

    // Parameterized constructor
    public UserBookmarkedArticleId(Integer memberNumber, Integer articleNumber) {
        this.memberNumber = memberNumber;
        this.articleNumber = articleNumber;
    }

    // hashCode and equals methods
}
