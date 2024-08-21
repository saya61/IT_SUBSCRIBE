package com.sw.journal.journalcrawlerpublisher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing  // JPA Auditing 활성화
public class JournalCrawlerPublisherApplication {

    public static void main(String[] args) {
        SpringApplication.run(JournalCrawlerPublisherApplication.class, args);
    }

}
