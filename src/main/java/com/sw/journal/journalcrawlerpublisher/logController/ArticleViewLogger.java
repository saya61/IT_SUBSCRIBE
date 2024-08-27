package com.sw.journal.journalcrawlerpublisher.logController;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArticleViewLogger {
    private static final Logger logger = LogManager.getLogger(ArticleViewLogger.class);

    // 조회된 기사 정보 및 사용자 정보를 저장하기 위한 로거
    public static void logView(
            String logType,         // 요청 유형 (이니셜로 쓰면 좋음)
            String logTime,         // 요청 시간
            String url,             // 요청 엔드포인트
            String method,          // HTTP METHODS (GET/POST/PUT/PATCH/DELETE)
            String memberId,        // 사용자 ID
            String articleId,       // productId : 상품 고유번호 (not null)
            String transactionId,   // 요청 고유값 (nullable)
            String memberIp,        // 사용자 IP
            String userAgent,       // 브라우저 등 요청에 사용된 SW 정보
            String referrer         // 직전 페이지 URL
    ) {
        logger.info(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", // 컬럼 개수 선언
                        logType,
                        logTime,
                        url,
                        method,
                        memberId,
                        articleId,
                        transactionId,
                        memberIp,
                        userAgent,
                        referrer
                )
        );
    }
}
