package com.sw.journal.journalcrawlerpublisher.domain;

import com.sw.journal.journalcrawlerpublisher.constant.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@Setter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Role role;

    @Column
    @ColumnDefault("false")
    private boolean alarm;

    @OneToOne
    @JoinColumn(name = "profile_image_id")
    private ProfileImage profileImage;

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", role='" + role + '\'' +
                '}';
    }
}
