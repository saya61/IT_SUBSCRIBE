package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.domain.CrawlingEvent;
import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.domain.UserFavoriteCategory;
import com.sw.journal.journalcrawlerpublisher.dto.ArticleWithTagsDTO;
import com.sw.journal.journalcrawlerpublisher.repository.CrawlingEventRepository;
import com.sw.journal.journalcrawlerpublisher.repository.TagRepository;
import com.sw.journal.journalcrawlerpublisher.repository.UserFavoriteCategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class AlarmServiceTest {
    @Autowired
    private CrawlingEventRepository crawlingEventRepository;
    @Autowired
    private UserFavoriteCategoryRepository userFavoriteCategoryRepository;
    @Autowired
    private TagService tagService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private MailService mailService;

    @Test
    public void sendAlarm() {
        // 처리 여부가 false 인 이벤트 조회
        List<CrawlingEvent> events = crawlingEventRepository.findByEventProcessed(false);

        // 이벤트 처리
        for (CrawlingEvent event : events) { // 트랜잭션 내에서 필요한 데이터를 미리 로드
            Article article = event.getArticle();
            // 해당 이벤트의 기사를 DTO 로 변환
            ArticleWithTagsDTO articleDTO = new ArticleWithTagsDTO(article.getId(),article.getTitle(), article.getCategory());

            // 신작 카테고리를 선호 카테고리로 등록한 회원 검색
            List<UserFavoriteCategory> userList = userFavoriteCategoryRepository.findByCategory(event.getCategory());

            // 이메일 발송 주소 리스트 생성
            List<String> recipientAddressList = userList.stream()
                    .map(UserFavoriteCategory::getMember)
                    .map(Member::getEmail)
                    .toList();

            // 이메일 발송
            for (String email : recipientAddressList) {
                mailService.sendAlarmMail(email, articleDTO);
            }

            // 이벤트 처리 여부 업데이트
            event.setEventProcessed(true); // 이벤트 처리 여부 false -> true
            crawlingEventRepository.save(event); // 크롤링 이벤트 테이블 업데이트
        }
    }
}