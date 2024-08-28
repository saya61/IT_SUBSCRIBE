package com.sw.journal.journalcrawlerpublisher.logController;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommentDeleteLogger {
    private static final Logger logger = LogManager.getLogger(CommentDeleteLogger.class);

    // 삭제되기 전 댓글의 내용을 저장하기 위한 로거
    public static void logView(
            String logType,         // 요청 유형 (이니셜로 쓰면 좋음)
            String logTime,         // 요청 시간
            String url,             // 요청 엔드포인트
            String method,          // HTTP METHODS (GET/POST/PUT/PATCH/DELETE)
            String commentId,       // 댓글 ID
            String articleId,       // 기사 ID
            String memberId,        // 사용자 ID
            String transactionId,   // 요청 고유값 (nullable)
            String content          // 댓글 삭제 전 내용
    ) {
        logger.info(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", // 컬럼 개수 선언
                        logType,
                        logTime,
                        url,
                        method,
                        commentId,
                        articleId,
                        memberId,
                        transactionId,
                        content
                )
        );
    }
}
