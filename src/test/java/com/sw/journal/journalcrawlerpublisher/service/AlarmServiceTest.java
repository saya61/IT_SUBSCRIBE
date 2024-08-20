package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.CrawlingEvent;
import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.domain.UserFavoriteCategory;;
import com.sw.journal.journalcrawlerpublisher.repository.CrawlingEventRepository;
import com.sw.journal.journalcrawlerpublisher.repository.UserFavoriteCategoryRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class AlarmServiceTest {
    @Autowired
    private CrawlingEventRepository crawlingEventRepository;
    @Autowired
    private UserFavoriteCategoryRepository userFavoriteCategoryRepository;
    @Autowired
    private MailService mailService;

    @Test
    @Transactional
    public void sendAlarm() {
            // 처리 여부가 false 인 이벤트 조회
            List<CrawlingEvent> events = crawlingEventRepository.findByIsEventProcessed(false);

            // 이벤트 처리
            for (CrawlingEvent event : events) {
                // 신작 카테고리를 선호 카테고리로 등록한 회원 검색
                List<UserFavoriteCategory> userList = userFavoriteCategoryRepository.findByCategory(event.getCategory());

                // 이메일 발송 주소 리스트 생성
                List<String> recipientAddressList = userList.stream()
                        .map(UserFavoriteCategory::getMember)
                        .map(Member::getEmail)
                        .toList();

                // 이메일 발송
                for (String email : recipientAddressList) {
                    mailService.sendAlarmMail(email, event.getArticle());
                }

                // 이벤트 처리 여부 업데이트
                event.setIsEventProcessed(true); // 이벤트 처리 여부 false -> true
                crawlingEventRepository.save(event); // 크롤링 이벤트 테이블 업데이트
            }
    }
}