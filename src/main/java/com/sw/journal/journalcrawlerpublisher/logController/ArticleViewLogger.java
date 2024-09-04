package com.sw.journal.journalcrawlerpublisher.logController;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArticleViewLogger {
    private static final Logger logger = LogManager.getLogger(ArticleViewLogger.class);

    // 조회된 기사 정보 및 사용자 정보를 저장하기 위한 로거
    public static void logArticleView(
            String logType,
            String logTime,
            String url,
            String method,
            String memberId,
            String articleId,
            String memberIp,
            String userAgent
    ) {
        logger.info(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                        logType,
                        logTime,
                        url,
                        method,
                        memberId != null ? memberId : "-",
                        articleId,
                        memberIp,
                        userAgent
                )
        );
    }
}
