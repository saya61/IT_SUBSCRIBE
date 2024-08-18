package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.constant.ReportReason;
import com.sw.journal.journalcrawlerpublisher.constant.Role;
import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.dto.CommentDTO;
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
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final MemberService memberService;

    // 댓글 생성
    @PostMapping
    public ResponseEntity<CommentDTO> createComment(@RequestBody CommentDTO commentDTO) {
        Member currentMember = getCurrentMember();
        // 댓글 DTO에 Member 정보 설정
        commentDTO.setMemberId(currentMember.getId());
        commentDTO.setMemberNickname(currentMember.getNickname());
        // ProfileImage가 null이 아닌 경우에만 설정
        if (currentMember.getProfileImage() != null) {
            commentDTO.setProfileImageURL(currentMember.getProfileImage().getFileUrl());
        }

        // 댓글을 생성하고 생성된 댓글 DTO를 반환
        CommentDTO createdComment = commentService.createComment(commentDTO);
        return ResponseEntity.ok(createdComment);
    }

    // 특정 기사에 대한 모든 댓글 조회
    @GetMapping("/article/{articleId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByArticle(@PathVariable Long articleId) {
        // 기사 ID로 댓글 리스트 조회
        List<CommentDTO> comments = commentService.getCommentsByArticle(articleId);
        // 조회된 댓글 리스트를 응답으로 반환
        return ResponseEntity.ok(comments);
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable Long commentId, @RequestBody CommentDTO commentDTO) {

        Member currentMember = getCurrentMember();
        verifyCommentOwner(commentId, currentMember);

        // 댓글 ID로 댓글 조회, 댓글 DTO로 댓글 정보 수정
        CommentDTO updatedComment = commentService.updateComment(commentId, commentDTO);
        // 수정된 댓글 DTO를 응답으로 반환
        return ResponseEntity.ok(updatedComment);
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {

        Member currentMember = getCurrentMember();

        // ADMIN 또는 SUPER_ADMIN 이면 자신의 댓글이 아니여도 삭제 가능함
        if (currentMember.getRole() != Role.ADMIN && currentMember.getRole() != Role.SUPER_ADMIN) {
            verifyCommentOwner(commentId, currentMember);
        }

        // 댓글 ID로 댓글 삭제
        commentService.deleteComment(commentId);
        // 삭제 성공 시 204 No Content 응답 반환
        return ResponseEntity.noContent().build();
    }

    // 댓글 신고
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
