package com.sw.journal.journalcrawlerpublisher.aspect;

import com.sw.journal.journalcrawlerpublisher.dto.MemberViewArticleDTO;
import com.sw.journal.journalcrawlerpublisher.logController.ArticleViewLogger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
public class LogAspect {

    @AfterReturning(pointcut = "execution(* com.sw.journal.journalcrawlerpublisher.controller.ArticleController.articleLogger(..))", returning = "result")
    public void afterArticleViewReturning(Object result) {
        MemberViewArticleDTO mva;
        if (result instanceof MemberViewArticleDTO) {
            mva = (MemberViewArticleDTO) result;
        }
        else{
            return;
        }

        ArticleViewLogger.logView(
                "ARTICLE_VIEW",
                LocalDateTime.now().toString(),
                "/views/"+mva.getArticleId().toString(),
                "POST",
                mva.getMemberId().toString(),
                mva.getArticleId().toString(),
                "-",
                mva.getMemberIp(),
                mva.getUserAgent(),
                mva.getReferrer()
        );
    }


//    @AfterReturning(pointcut = "execution(* com.sw.journal.journalcrawlerpublisher.logController.LoggingController.createMVALog(..))", returning = "result")
//    public void afterArticleViewReturning(Object result) {
//        MemberViewArticleDTO mva = (MemberViewArticleDTO) result;  // DTO 추출
//
//        ArticleViewLogger.logView(
//                "ARTICLE_VIEW",
//                LocalDateTime.now().toString(),
//                "/views/"+mva.getArticleId().toString(),
//                "POST",
//                mva.getMemberId().toString(),
//                mva.getArticleId().toString(),
//                "-",
//                mva.getRequest()
//        );
//    }
}
