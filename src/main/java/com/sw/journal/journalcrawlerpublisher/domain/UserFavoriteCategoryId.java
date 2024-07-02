package com.sw.journal.journalcrawlerpublisher.domain;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class UserFavoriteCategoryId implements Serializable {
    private Integer memberNumber;
    private Integer categoryCode;

    // Default constructor
    public UserFavoriteCategoryId() {}

    // Parameterized constructor
    public UserFavoriteCategoryId(Integer memberNumber, Integer categoryCode) {
        this.memberNumber = memberNumber;
        this.categoryCode = categoryCode;
    }
}
