package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.constant.ReportReason;
import com.sw.journal.journalcrawlerpublisher.constant.Role;
import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.dto.CommentDTO;
import com.sw.journal.journalcrawlerpublisher.dto.ReplyDTO;
import com.sw.journal.journalcrawlerpublisher.dto.ReportDTO;
import com.sw.journal.journalcrawlerpublisher.exception.UnauthorizedException;
import com.sw.journal.journalcrawlerpublisher.service.CommentService;
import com.sw.journal.journalcrawlerpublisher.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final MemberService memberService;

    // 1. 댓글 생성
    @PostMapping
    public ResponseEntity<CommentDTO> createComment(@RequestBody CommentDTO commentDTO) {
        // 댓글을 생성하고 생성된 댓글 DTO 를 반환
        CommentDTO createdComment = commentService.createComment(commentDTO);
        return ResponseEntity.ok(createdComment);
    }

    // 2. 대댓글 생성
    @PostMapping("/{commentId}/reply")
    public ResponseEntity<ReplyDTO> createReply(@PathVariable Long commentId, @RequestBody ReplyDTO replyDTO) {
        replyDTO.setParentCommentId(commentId);
        ReplyDTO createdReply = commentService.createReply(replyDTO);
        return ResponseEntity.ok(createdReply);
    }

    // 3. 특정 기사에 대한 모든 최상위 댓글 조회
    @GetMapping("/articles/{articleId}/comments")
    public ResponseEntity<List<CommentDTO>> getCommentsByArticle(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "likes") String filter) { // 디폴트는 좋아요 순

        List<CommentDTO> comments = commentService.getCommentsByArticle(articleId, filter);
        return ResponseEntity.ok(comments);
    }

    // 4. 특정 댓글에 달린 모든 대댓글 조회
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<ReplyDTO>> getRepliesByCommentId(@PathVariable Long commentId) {
        List<ReplyDTO> replies = commentService.getRepliesByCommentId(commentId);
        return ResponseEntity.ok(replies);
    }

    // 3-2. 특정 기사에 대한 모든 댓글 조회
    @GetMapping("/article/{articleId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByArticle(@PathVariable Long articleId) {
        // 기사 ID로 댓글 리스트 조회
        List<CommentDTO> comments = commentService.getCommentsByArticle(articleId);
        // 조회된 댓글 리스트를 응답으로 반환
        return ResponseEntity.ok(comments);
    }

    // 5. 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable Long commentId, @RequestBody CommentDTO commentDTO) {
        // 댓글 ID로 댓글 조회, 댓글 DTO 로 댓글 정보 수정
        CommentDTO updatedComment = commentService.updateComment(commentId, commentDTO);
        // 수정된 댓글 DTO 를 응답으로 반환
        return ResponseEntity.ok(updatedComment);
    }

    // 6. 대댓글 수정
    @PutMapping("/reply/{replyId}")
    public ResponseEntity<ReplyDTO> updateReply(@PathVariable Long replyId, @RequestBody ReplyDTO replyDTO) {
        ReplyDTO updatedReply = commentService.updateReply(replyId, replyDTO);
        return ResponseEntity.ok(updatedReply);
    }

    // 7. 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        // 댓글 ID로 댓글 삭제
        commentService.deleteComment(commentId);
        // 삭제 성공 시 204 No Content 응답 반환
        return ResponseEntity.noContent().build();
    }

    // 8. 대댓글 삭제
    @DeleteMapping("/reply/{replyId}")
    public ResponseEntity<Void> deleteReply(@PathVariable Long replyId) {
        commentService.deleteReply(replyId);
        return ResponseEntity.noContent().build();
    }

    // 9. 댓글 좋아요 토글
    @PostMapping("/{commentId}/like")
    public ResponseEntity<CommentDTO> toggleLikeComment(@PathVariable Long commentId) {
        CommentDTO updatedComment = commentService.toggleLikeComment(commentId);
        return ResponseEntity.ok(updatedComment);
    }

    // 10. 대댓글 좋아요 토글
    @PostMapping("/reply/{replyId}/like")
    public ResponseEntity<ReplyDTO> toggleLikeReply(@PathVariable Long replyId) {
        ReplyDTO updatedReply = commentService.toggleLikeReply(replyId);
        return ResponseEntity.ok(updatedReply);
    }

    // 11. 댓글 신고
    @PostMapping("/{commentId}/report")
    public ResponseEntity<ReportDTO.Request> reportComment(
            @PathVariable Long commentId,
            @RequestBody ReportDTO.Request reportRequest) {

        Member currentMember = getCurrentMember();          // 현재 로그인한 사용자 정보 조회
        reportRequest.setReporterId(currentMember.getId()); // 신고자 ID 설정
        reportRequest.setCommentId(commentId);              // 댓글 ID 설정

        commentService.reportComment(reportRequest);        // 신고 처리 로직
        return ResponseEntity.ok(reportRequest);            // 처리된 요청 데이터 반환 (확인용)
    }

    // 현재 로그인한 사용자의 Member 객체를 가져오는 메서드
    private Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        return memberService.findByUsername(currentUsername)
                .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다"));
    }

    // 댓글 소유자 검증
    private void verifyCommentOwner(Long commentId, Member member) {
        if (!commentService.isCommentOwner(commentId, member.getUsername())) {
            throw new UnauthorizedException("수정 권한이 없습니다");
        }
    }

}
