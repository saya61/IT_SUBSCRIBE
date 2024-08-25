//package com.sw.journal.journalcrawlerpublisher.service;
//
//import com.sw.journal.journalcrawlerpublisher.domain.Article;
//import com.sw.journal.journalcrawlerpublisher.domain.Category;
//import com.sw.journal.journalcrawlerpublisher.domain.Member;
//import com.sw.journal.journalcrawlerpublisher.domain.UserFavoriteCategory;;
//import com.sw.journal.journalcrawlerpublisher.dto.CrawlingEventDTO;
//import com.sw.journal.journalcrawlerpublisher.repository.ArticleRepository;
//import com.sw.journal.journalcrawlerpublisher.repository.CategoryRepository;
//import com.sw.journal.journalcrawlerpublisher.repository.UserFavoriteCategoryRepository;
//import jakarta.transaction.Transactional;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.support.Acknowledgment;
//
//import java.util.List;
//
//@SpringBootTest
//class AlarmServiceTest {
//    @Autowired
//    private UserFavoriteCategoryRepository userFavoriteCategoryRepository;
//    @Autowired
//    private MailService mailService;
//    @Autowired
//    private CategoryRepository categoryRepository;
//    @Autowired
//    private ArticleRepository articleRepository;
//
//    @Test
//    @KafkaListener(topics = "${spring.kafka.template.default-topic}"
//            , groupId = "${spring.kafka.consumer.group-id}"
//            // properties 속성을 통해 수신한 JSON 메시지를 CrawlingEventDto 로 자동으로 역직렬화
//            , properties = {"spring.json.value.default.type:com.sw.journal.journalcrawlerpublisher.dto.CrawlingEventDTO"}
//            )
//    public void listen(CrawlingEventDTO eventDTO, Acknowledgment ack) {
//        // Kafka 에서 DTO 를 수신 받아 categoryId와 articleId 추출
//        Long categoryId = eventDTO.getCategoryId();
//        Long articleId = eventDTO.getArticleId();
//
//        // categoryId로 Category 객체 조회
//        Category category = categoryRepository.findById(categoryId).orElse(null);
//        // articleId로 Article 객체 조회
//        Article article = articleRepository.findById(articleId).orElse(null);
//
//        if (category != null && article != null) {
//            // 신작 카테고리를 선호 카테고리로 등록한 회원 검색
//            List<UserFavoriteCategory> userList = userFavoriteCategoryRepository.findByCategory(category);
//
//            // 이메일 발송 주소 리스트 생성
//            List<String> recipientAddressList = userList.stream()
//                    .map(UserFavoriteCategory::getMember)
//                    .map(Member::getEmail)
//                    .toList();
//
//            // 이메일 발송
//            boolean allEmailsSent = true; // 이메일 발송 플래그
//            for (String email : recipientAddressList) {
//                try {
//                    mailService.sendAlarmMail(email, article);
//                } catch (Exception e) {
//                    allEmailsSent = false; // 이메일 발송 실패 시 플래그 설정
//                    e.printStackTrace();
//                }
//            }
//
//            // 모든 이메일 발송 성공
//            if (allEmailsSent) {
//                // 오프셋 수동 커밋
//                ack.acknowledge();
//            } else { // 모든 이메일 발송에 실패한 경우
//
//            }
//        }
//    }
//}