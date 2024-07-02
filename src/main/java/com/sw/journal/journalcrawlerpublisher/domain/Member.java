package com.sw.journal.journalcrawlerpublisher.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "member")
@Getter
@Setter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_number")
    private Integer memberNumber;

    @Column(name = "member_id", nullable = false, length = 30)
    private String memberId;

    @Column(name = "member_password", nullable = false, length = 255)
    private String memberPassword;

    @Column(name = "member_nickname", nullable = false, length = 30)
    private String memberNickname;

    @Column(name = "member_email", nullable = false, length = 50)
    private String memberEmail;
}
