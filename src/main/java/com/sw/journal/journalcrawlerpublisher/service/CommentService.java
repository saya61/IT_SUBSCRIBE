package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.constant.CommentReportStatus;
import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.domain.Comment;
import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.domain.Report;
import com.sw.journal.journalcrawlerpublisher.dto.CommentDTO;
import com.sw.journal.journalcrawlerpublisher.dto.ReportDTO;
import com.sw.journal.journalcrawlerpublisher.repository.ArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.CommentRepository;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import com.sw.journal.journalcrawlerpublisher.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;
    private final ReportRepository reportRepository;

    // 댓글 생성
    public CommentDTO createComment(CommentDTO commentDTO) {
        // 댓글을 작성한 기사 조회
        Article article = articleRepository.findById(commentDTO.getArticleId())
                // 존재하지 않으면 예외 발생
                .orElseThrow(() -> new IllegalArgumentException("Invalid article ID"));

        // 댓글을 작성한 회원 조회
        Member member = memberRepository.findById(commentDTO.getMemberId())
                // 존재하지 않으면 예외 발생
                .orElseThrow(() -> new IllegalArgumentException("Invalid member ID"));

        // 새로운 댓글 객체 생성 및 설정
        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent()); // 댓글 내용 설정
        comment.setArticle(article); // 댓글을 작성한 기사 설정
        comment.setMember(member); // 댓글 작성자 설정
        comment = commentRepository.save(comment); // 댓글을 DB에 저장

        return mapToDTO(comment); // 생성된 댓글을 DTO 로 변환하여 반환
    }

    // 특정 기사에 대한 모든 댓글 조회
    public List<CommentDTO> getCommentsByArticle(Long articleId) {
        // 기사 조회
        Article article = articleRepository.findById(articleId)
                // 존재하지 않으면 예외 발생
                .orElseThrow(() -> new IllegalArgumentException("Invalid article ID"));

        // 특정 기사에 달린 모든 댓글을 조회하여 DTO로 변환 후 리스트로 반환
        return commentRepository.findByArticle(article)
                .stream() // 스트림으로 변환하여 각 댓글에 대해 작업 수행
                .map(this::mapToDTO) // 각 댓글을 DTO로 변환
                .collect(Collectors.toList()); // 결과를 리스트로 수집
    }

    // 특정 댓글 수정
    public CommentDTO updateComment(Long commentId, CommentDTO commentDTO) {
        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                // 존재하지 않으면 예외 발생
                .orElseThrow(() -> new IllegalArgumentException("Invalid comment ID"));

        comment.setContent(commentDTO.getContent()); // 댓글 내용 수정
        comment = commentRepository.save(comment); // 수정된 댓글을 DB에 저장

        return mapToDTO(comment); // 수정된 댓글을 DTO 로 변환하여 반환
    }

    // 특정 댓글 삭제
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId); // DB 에서 댓글 삭제
    }

    // 댓글 신고
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

    // Comment 엔티티를 CommentDTO 로 변환
    private CommentDTO mapToDTO(Comment comment) {
        CommentDTO commentDTO = new CommentDTO(); // DTO 객체 생성
        commentDTO.setId(comment.getId()); // 댓글 ID 설정
        commentDTO.setContent(comment.getContent()); // 댓글 내용 설정
        commentDTO.setArticleId(comment.getArticle().getId()); // 댓글을 작성한 기사 ID 설정
        commentDTO.setMemberId(comment.getMember().getId()); // 댓글 작성자 ID 설정
        commentDTO.setMemberNickname(comment.getMember().getNickname()); // 댓글 작성자 닉네임 설정
        // 댓글 작성자의 프로필 이미지 URL 설정 (프로필 이미지가 존재하는 경우)
        commentDTO.setProfileImageURL(comment.getMember().getProfileImage() != null ? comment.getMember().getProfileImage().getFileUrl() : null);
        return commentDTO; // 변환된 DTO 반환
    }

    // 댓글 소유자 검증
    public boolean isCommentOwner(Long commentId, String currentUsername) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid comment ID"));
        return comment.getMember().getUsername().equals(currentUsername);
    }


}