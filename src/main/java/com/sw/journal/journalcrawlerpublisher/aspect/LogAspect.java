package com.sw.journal.journalcrawlerpublisher.aspect;

import com.sw.journal.journalcrawlerpublisher.domain.Comment;
import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.logController.ArticleSearchLogger;
import com.sw.journal.journalcrawlerpublisher.logController.ArticleViewLogger;
import com.sw.journal.journalcrawlerpublisher.logController.CommentDeleteLogger;
import com.sw.journal.journalcrawlerpublisher.repository.CommentRepository;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    public Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 로그인한 사용자의 id를 가져옴
        String currentUsername = authentication.getName();
        // 사용자 id로 Member 객체 조회
        Optional<Member> member = memberRepository.findByUsername(currentUsername);

        // 사용자가 존재하지 않는 경우 401 Unauthorized 응답 반환
        if (member.isEmpty()) {
            return null;
        }
        // 조회된 Member 객체에서 정보 추출
        return member.get();
    }

    @AfterReturning(pointcut = "execution(* com.sw.journal.journalcrawlerpublisher.controller.ArticleController.memberReadArticle(..))")
    public void afterArticleViewReturning(JoinPoint joinPoint) {
        Long articleId = Long.parseLong(joinPoint.getArgs()[0].toString());
        HttpServletRequest request = (HttpServletRequest) joinPoint.getArgs()[1];
        Member currentMember = getCurrentMember();
        if (currentMember == null) {
            return;
        }

        ArticleViewLogger.logArticleView(
                "ARTICLE_VIEW",
                LocalDateTime.now().toString(),
                "/article/view/"+articleId,
                "GET",
                currentMember.getId().toString(),
                articleId.toString(),
                "-",
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                request.getHeader("Referrer")
        );
    }

    @Before("execution(* com.sw.journal.journalcrawlerpublisher.controller.CommentController.deleteComment(..))")
    public void beforeCommentDelete(JoinPoint joinPoint) {
        Long commentId = (Long) joinPoint.getArgs()[0];
        Comment comment;
        if(commentRepository.findById(commentId).isPresent()){
            comment = commentRepository.findById(commentId).get();
        }
        else {
            return;
        }
        CommentDeleteLogger.logCommentDelete(
                "COMMENT_DELETE",
                LocalDateTime.now().toString(),
                "/api/comment/"+comment.getId().toString(),
                "DELETE",
                comment.getId().toString(),
                comment.getArticle().getId().toString(),
                comment.getMember().getId().toString(),
                "-",
                comment.getContent()
        );
    }

    @AfterReturning(pointcut = "execution(* com.sw.journal.journalcrawlerpublisher.controller.ArticleController.searchArticles(..))")
    public void afterArticleSearchReturning(JoinPoint joinPoint) {
        String keyWords = joinPoint.getArgs()[0].toString();
        HttpServletRequest request = (HttpServletRequest) joinPoint.getArgs()[3];
        String currentMemberId;
        if(getCurrentMember() == null){
            currentMemberId = null;
        }
        else {
            currentMemberId = getCurrentMember().getId().toString();
        }
        ArticleSearchLogger.logArticleSearch(
                "ARTICLE_SEARCH",
                LocalDateTime.now().toString(),
                "/article/search/"+keyWords,
                "GET",
                currentMemberId,
                keyWords,
                "-",
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                request.getHeader("Referrer")
        );

    }

}
