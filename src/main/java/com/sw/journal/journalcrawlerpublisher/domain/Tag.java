package com.sw.journal.journalcrawlerpublisher.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tag")
@Getter
@Setter
public class Tag {
    @Id
    @Column(name = "tag_code", length = 30)
    private String tagCode;

    @Column(name = "tag_name", nullable = false, length = 30)
    private String tagName;

    @ManyToOne
    @JoinColumn(name = "category_code")
    private Category category;
}
