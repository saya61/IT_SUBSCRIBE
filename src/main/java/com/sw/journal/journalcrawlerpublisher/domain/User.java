package com.sw.journal.journalcrawlerpublisher.domain;

import com.sw.journal.journalcrawlerpublisher.constant.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "member2")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId;

    @Column(unique = true, nullable = false)
    private String userNickname;

    @Column(nullable = false)
    private String userPw;

    @Column(unique = true, nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private Date userCreatedAt;

    @Column(nullable = false)
    private UserRole userRole;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", userNickname='" + userNickname + '\'' +
                ", userPw='" + userPw + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userCreatedAt=" + userCreatedAt +
                ", userRole=" + userRole +
                '}';
    }
}
