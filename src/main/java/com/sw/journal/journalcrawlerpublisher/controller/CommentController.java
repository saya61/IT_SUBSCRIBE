package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.dto.CommentDTO;
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
        // 현재 로그인한 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 로그인한 사용자의 id를 가져옴
        String currentUsername = authentication.getName();
        // 사용자 id로 Member 객체 조회
        Optional<Member> member = memberService.findByUsername(currentUsername);

        // 사용자가 존재하지 않는 경우 401 Unauthorized 응답 반환
        if (!member.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 조회된 Member 객체에서 정보 추출
        Member foundMember = member.get();
        // 댓글 DTO에 Member 정보 설정
        commentDTO.setMemberId(foundMember.getId());
        commentDTO.setMemberNickname(foundMember.getNickname());
        commentDTO.setProfileImageURL(foundMember.getProfileImage().getFileUrl());

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
        // 댓글 ID로 댓글 조회, 댓글 DTO로 댓글 정보 수정
        CommentDTO updatedComment = commentService.updateComment(commentId, commentDTO);
        // 수정된 댓글 DTO를 응답으로 반환
        return ResponseEntity.ok(updatedComment);
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        // 댓글 ID로 댓글 삭제
        commentService.deleteComment(commentId);
        // 삭제 성공 시 204 No Content 응답 반환
        return ResponseEntity.noContent().build();
    }
}
