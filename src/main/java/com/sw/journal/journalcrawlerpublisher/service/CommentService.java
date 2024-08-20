package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.constant.CommentReportStatus;
import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.domain.Comment;
import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.domain.Reply;
import com.sw.journal.journalcrawlerpublisher.domain.Report;
import com.sw.journal.journalcrawlerpublisher.dto.CommentDTO;
import com.sw.journal.journalcrawlerpublisher.dto.ReplyDTO;
import com.sw.journal.journalcrawlerpublisher.dto.ReportDTO;
import com.sw.journal.journalcrawlerpublisher.repository.ArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.CommentRepository;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import com.sw.journal.journalcrawlerpublisher.repository.ReplyRepository;
import com.sw.journal.journalcrawlerpublisher.util.TimeUtils;
import com.sw.journal.journalcrawlerpublisher.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Comparator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    // 의존성 주입 ( by 생성자 주입 )
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;
    private final ReplyRepository replyRepository;
    // private final UserSettingsRepository userSettingsRepository; // 알림 설정을 위한 Repository
    private final ReportRepository reportRepository;

    // 0.  현재 인증된 사용자 가져오기
    private Member getAuthenticatedMember() {
        // 현재 로그인한 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 로그인한 사용자의 id를 가져옴
        String currentUsername = authentication.getName();
        return memberRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("현재 로그인한 사용자를 찾을 수 없습니다."));
    }

    // 1. 댓글 생성
    public CommentDTO createComment(CommentDTO commentDTO) {
        // 댓글을 작성한 기사 조회
        Article article = articleRepository.findById(commentDTO.getArticleId())
                // 존재하지 않으면 예외 발생
                .orElseThrow(() -> new IllegalArgumentException("Invalid article ID"));

        // 댓글을 작성한 회원 조회
        Member member = getAuthenticatedMember();

        // 새로운 댓글 객체 생성 및 설정
        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent()); // 댓글 내용 설정
        comment.setArticle(article); // 댓글을 작성한 기사 설정
        comment.setMember(member); // 댓글 작성자 설정
        // 알림 설정
        // comment.setNotificationEnabled(commentDTO.isNotificationEnabled());
        comment = commentRepository.save(comment); // 댓글을 DB에 저장

        return mapToDTO(comment); // 생성된 댓글을 DTO 로 변환하여 반환
    }

    // 2. 대댓글 생성
    public ReplyDTO createReply(ReplyDTO replyDTO) {
        // 부모 댓글을 찾을 수 없을때 예외처리
        Comment parentComment = commentRepository.findById(replyDTO.getParentCommentId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid parent comment ID"));

        Member member = getAuthenticatedMember();

        // Reply 엔티티 사용
        Reply reply = new Reply();
        reply.setContent(replyDTO.getContent());
        reply.setMember(member);
        reply.setParentComment(parentComment); // 부모 댓글 설정

        reply = replyRepository.save(reply); // Reply Repository 사용

        return mapToReplyDTO(reply);
    }

    // 3. 특정 기사에 대한 모든 최상위 댓글 조회 (대댓글 제외, 대댓글 수만 포함)
    public List<CommentDTO> getCommentsByArticle(Long articleId, String filter) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid article ID"));

        if (filter == null || filter.isEmpty()) {
            filter = "likes"; // 기본값 설정
        }

        return commentRepository.findByArticleAndParentCommentIsNull(article)
                .stream()
                .map(comment -> {
                    CommentDTO commentDTO = mapToDTO(comment);
                    commentDTO.setReplyCount(commentRepository.countByParentComment(comment));
                    return commentDTO;
                })
                .sorted(getComparatorForFilter(filter))
                .collect(Collectors.toList());
    }

    // 오버로딩 메서드: 하나의 파라미터만 받는 메서드
    public List<CommentDTO> getCommentsByArticle(Long articleId) {
        return getCommentsByArticle(articleId, "likes"); // 기본 필터를 "likes"로 설정
    }

    // 4. 특정 댓글에 달린 모든 대댓글 조회 (항상 최신순으로 정렬)
    public List<ReplyDTO> getRepliesByCommentId(Long commentId) {
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid comment ID"));

        return replyRepository.findByParentCommentOrderByCreatedAtDesc(parentComment) // 최신순 정렬
                .stream()
                .map(this::mapToReplyDTO)
                .collect(Collectors.toList());
    }


    // 5. 특정 댓글 수정
    public CommentDTO updateComment(Long commentId, CommentDTO commentDTO) {
        Member member = getAuthenticatedMember();
        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                // 존재하지 않으면 예외 발생
                .orElseThrow(() -> new IllegalArgumentException("Invalid comment ID"));

        // 댓글 작성자 확인
        if (!comment.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("You are not authorized to update this comment.");
        }

        comment.setContent(commentDTO.getContent()); // 댓글 내용 수정
        comment = commentRepository.save(comment); // 수정된 댓글을 DB에 저장

        return mapToDTO(comment); // 수정된 댓글을 DTO 로 변환하여 반환
    }

    // 6. 대댓글 수정
    public ReplyDTO updateReply(Long replyId, ReplyDTO replyDTO) {
        Member member = getAuthenticatedMember();
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reply ID"));

        // 작성자가 맞는지 확인
        if (!reply.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("You do not have permission to update this reply.");
        }

        // 내용 수정
        reply.setContent(replyDTO.getContent());
        reply = replyRepository.save(reply);

        return mapToReplyDTO(reply);
    }

    // 7. (최상위) 특정 댓글 삭제
    public void deleteComment(Long commentId) {
        Member member = getAuthenticatedMember();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid comment ID"));

        // 댓글 작성자 확인
        if (!comment.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("You are not authorized to delete this comment.");
        }

        // 대댓글이 없는 최상위 댓글인지 확인
        if (comment.getParentComment() == null && replyRepository.countByParentComment(comment) > 0) {
            // 최상위 댓글이고 대댓글이 존재하면, 내용과 닉네임을 변경
            comment.setContent("삭제된 댓글입니다.");
            // comment.getMember().setNickname("익명");
            // TODO : React 에서 이 부분은
            commentRepository.save(comment);
        } else {
            // 대댓글이거나 최상위 댓글에 대댓글이 없는 경우 바로 삭제
            commentRepository.delete(comment);
        }
    }

    // 8. 대댓글 삭제
    public void deleteReply(Long replyId) {
        Member member = getAuthenticatedMember();
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reply ID"));

        // 작성자가 맞는지 확인
        if (!reply.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("You do not have permission to delete this reply.");
        }

        // 대댓글 삭제
        replyRepository.delete(reply);
    }


    // Comment 엔티티를 CommentDTO 로 변환
    private CommentDTO mapToDTO(Comment comment) {
        CommentDTO commentDTO = new CommentDTO(); // DTO 객체 생성
        commentDTO.setId(comment.getId()); // 댓글 ID 설정
        commentDTO.setContent(comment.getContent()); // 댓글 내용 설정
        commentDTO.setArticleId(comment.getArticle().getId()); // 댓글을 작성한 기사 ID 설정
        commentDTO.setMemberId(comment.getMember().getId()); // 댓글 작성자 ID 설정
        commentDTO.setMemberNickname(comment.getMember().getNickname()); // 댓글 작성자 닉네임 설정
        commentDTO.setLikeCount(comment.getLikeCount()); // 좋아요 수 설정
        commentDTO.setRelativeTime(TimeUtils.getRelativeTime(comment.getCreatedAt()));

        // 댓글 작성자의 프로필 이미지 URL 설정 (프로필 이미지가 존재하는 경우)
        if (comment.getMember().getProfileImage() != null) {
            commentDTO.setProfileImageURL(comment.getMember().getProfileImage().getFileUrl());
        } else {
            // TODO : Default image 수정
            commentDTO.setProfileImageURL("default_image_url");
        }

        return commentDTO; // 변환된 DTO 반환
    }


    // Comment 엔티티를 ReplyDTO 로 변환
    private ReplyDTO mapToReplyDTO(Reply comment) {
        ReplyDTO replyDTO = new ReplyDTO();
        replyDTO.setId(comment.getId());
        replyDTO.setContent(comment.getContent());
        replyDTO.setMemberId(comment.getMember().getId());
        replyDTO.setMemberNickname(comment.getMember().getNickname());
        replyDTO.setLikeCount(comment.getLikeCount());
        replyDTO.setParentCommentId(comment.getParentComment().getId());
        replyDTO.setRelativeTime(TimeUtils.getRelativeTime(comment.getCreatedAt()));

        if (comment.getMember().getProfileImage() != null) {
            replyDTO.setProfileImageURL(comment.getMember().getProfileImage().getFileUrl());
        } else {
            replyDTO.setProfileImageURL("default_image_url");
        }
        return replyDTO;
    }

    // 필터에 따른 정렬 Comparator 제공
    private Comparator<CommentDTO> getComparatorForFilter(String filter) {
        return switch (filter) {
            case "newest" -> Comparator.comparing(CommentDTO::getId).reversed(); // 최신순 (ID가 높을수록 최신)
            case "oldest" -> Comparator.comparing(CommentDTO::getId); // 오래된 순
            case "likes" -> Comparator.comparing(CommentDTO::getLikeCount).reversed(); // 좋아요 수 많은 순
            default -> Comparator.comparing(CommentDTO::getLikeCount).reversed(); // 기본값 (좋아요 순)
        };
    }

    // 9. 댓글 좋아요 토글
    public CommentDTO toggleLikeComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid comment ID"));
        Member member = getAuthenticatedMember();


        // 좋아요 여부 확인 및 토글
        if (comment.getLikedBy().contains(member)) {
            comment.getLikedBy().remove(member);        // 사용자가 이미 좋아요를 누른 경우, 좋아요를 취소
            comment.decrementLikeCount();               // 좋아요 수 감소
        } else {
            comment.getLikedBy().add(member);           // 사용자가 좋아요를 누르지 않은 경우, 좋아요 추가
            comment.incrementLikeCount();               // 좋아요 수 증가
        }

        comment = commentRepository.save(comment);
        return mapToDTO(comment);
    }

    // 10. 대댓글 좋아요 토글
    public ReplyDTO toggleLikeReply(Long replyId) {
        Member member = getAuthenticatedMember();
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reply ID"));

        // 좋아요 여부 확인 및 토글
        if (reply.getLikedBy().contains(member)) {
            reply.getLikedBy().remove(member);  // 사용자가 이미 좋아요를 누른 경우, 좋아요를 취소
            reply.decrementLikeCount();         // 좋아요 수 감소
        } else {
            reply.getLikedBy().add(member);     // 사용자가 좋아요를 누르지 않은 경우, 좋아요 추가
            reply.incrementLikeCount();         // 좋아요 수 증가
        }

        replyRepository.save(reply);
        return mapToReplyDTO(reply);
    }

    // 11. 댓글 신고
    public void reportComment(ReportDTO.Request reportRequest) {
        // Report 객체 생성
        Report report = new Report();
        // Report 객체 set
        report.setComment(commentRepository.findById(reportRequest.getCommentId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid comment ID")));
        report.setReporter(memberRepository.findById(reportRequest.getReporterId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Member ID")));
        report.setReason(reportRequest.getReason());
        report.setReportDate(LocalDateTime.now());
        report.setStatus(CommentReportStatus.PENDING);
        // Report 객체 저장
        reportRepository.save(report);
    }


    // 댓글 소유자 검증
    public boolean isCommentOwner(Long commentId, String currentUsername) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid comment ID"));
        return comment.getMember().getUsername().equals(currentUsername);
    }

}