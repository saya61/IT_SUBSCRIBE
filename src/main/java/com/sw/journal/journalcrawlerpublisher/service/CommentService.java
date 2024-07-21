package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.Comment;
import com.sw.journal.journalcrawlerpublisher.domain.OurArticle;
import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.dto.CommentDTO;
import com.sw.journal.journalcrawlerpublisher.repository.CommentRepository;
import com.sw.journal.journalcrawlerpublisher.repository.OurArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final OurArticleRepository articleRepository;
    private final MemberRepository memberRepository;

    public CommentDTO createComment(CommentDTO commentDTO) {
        OurArticle article = articleRepository.findById(commentDTO.getArticleId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid article ID"));

        Member member = memberRepository.findById(commentDTO.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid member ID"));

        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setArticle(article);
        comment.setMember(member);
        comment = commentRepository.save(comment);

        return mapToDTO(comment);
    }

    public List<CommentDTO> getCommentsByArticle(Long articleId) {
        OurArticle article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid article ID"));
        return commentRepository.findByArticle(article)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CommentDTO updateComment(Long commentId, CommentDTO commentDTO) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid comment ID"));

        comment.setContent(commentDTO.getContent());
        comment = commentRepository.save(comment);

        return mapToDTO(comment);
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    private CommentDTO mapToDTO(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getId());
        commentDTO.setContent(comment.getContent());
        commentDTO.setArticleId(comment.getArticle().getId());
        commentDTO.setMemberId(comment.getMember().getId());
        commentDTO.setMemberNickname(comment.getMember().getNickname());
        commentDTO.setProfileImageURL(comment.getMember().getProfileImage() != null ? comment.getMember().getProfileImage().getFileUrl() : null);
        return commentDTO;
    }
}
