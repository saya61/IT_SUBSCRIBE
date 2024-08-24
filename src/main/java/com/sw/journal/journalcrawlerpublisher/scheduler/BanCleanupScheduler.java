package com.sw.journal.journalcrawlerpublisher.scheduler;

import com.sw.journal.journalcrawlerpublisher.domain.Ban;
import com.sw.journal.journalcrawlerpublisher.repository.BanRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class BanCleanupScheduler {

    private final BanRepository banRepository;

    public BanCleanupScheduler(BanRepository banRepository) {
        this.banRepository = banRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    public void cleanUpExpiredBans() {
        LocalDate now = LocalDate.now();
        List<Ban> expiredBans = banRepository.findByActiveTrueAndBanEndDateBefore(now);

        for (Ban ban : expiredBans) {
            ban.setActive(false);
            banRepository.save(ban);
        }
    }
}
