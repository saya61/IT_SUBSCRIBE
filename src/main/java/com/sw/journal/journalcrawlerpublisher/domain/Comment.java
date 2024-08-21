package com.sw.journal.journalcrawlerpublisher.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

// 최상위 댓글
@Entity
@Table(name = "comment")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class) // Auditing 기능 활성화 ( 엔티티의 생성 시간, 수정 시간 등을 자동으로 관리 )
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 대댓글을 위해 추가된 부분
    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    // 댓글 삭제 여부
    @Column(nullable = false)
    private boolean isDeleted = false;      // 삭제 여부


    @ManyToMany
    @JoinTable(
            name = "comment_likes",
            joinColumns = @JoinColumn(name = "comment_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private Set<Member> likedBy = new HashSet<>(); // 좋아요를 누른 사용자들

    @Column(nullable = false)
    private int likeCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdAt;

    // 알림 설정 필드 추가
    // @Column(name = "is_notification_enabled")
    // private boolean isNotificationEnabled;

    // 댓글 삭제 처리 메서드
    public void markAsDeleted() {
        this.content = "삭제된 댓글입니다.";
        this.isDeleted = true;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        this.likeCount--;
    }

}
