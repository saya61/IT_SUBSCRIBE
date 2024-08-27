package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.Category;
import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.domain.UserFavoriteCategory;;
import com.sw.journal.journalcrawlerpublisher.dto.CrawlingEventDTO;
import com.sw.journal.journalcrawlerpublisher.repository.CategoryRepository;
import com.sw.journal.journalcrawlerpublisher.repository.UserFavoriteCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
class AlarmService {

    private final UserFavoriteCategoryRepository userFavoriteCategoryRepository;
    private final MailService mailService;
    private final CategoryRepository categoryRepository;

    // Kafka 에서 크롤링 이벤트에 대한 메시지를 수신하는 메서드
    @KafkaListener(topics = "${spring.kafka.template.default-topic}"
            , groupId = "${spring.kafka.consumer.group-id}"
            // properties 속성을 통해 수신한 JSON 메시지를 CrawlingEventDto 로 자동으로 역직렬화
            , properties = {"spring.json.value.default.type:com.sw.journal.journalcrawlerpublisher.dto.CrawlingEventDTO"}
    )
    public void listenCrawlingEvent(CrawlingEventDTO eventDTO, Acknowledgment ack) {
        // Kafka 에서 DTO 를 수신 받아 categoryId와 articleId 추출
        Long categoryId = eventDTO.getCategoryId();
        Long articleId = eventDTO.getArticleId();

        // categoryId로 Category 객체 조회
        Category category = categoryRepository.findById(categoryId).orElse(null);

        if (category != null) {
            // 신작 카테고리를 선호 카테고리로 등록한 회원 검색
            List<UserFavoriteCategory> userList = userFavoriteCategoryRepository.findByCategory(category);
            System.out.println("userList : " + userList);

            // 이메일 발송 주소 리스트 생성
            List<String> recipientAddressList = userList.stream()
                    .map(UserFavoriteCategory::getMember)
                    .map(Member::getEmail)
                    .toList();
            System.out.println("recipientAddressList : " + recipientAddressList);

            // 이메일 발송
            boolean allEmailsSent = true; // 이메일 발송 플래그
            for (String email : recipientAddressList) {
                try {
                    mailService.sendAlarmMail(email, articleId);
                } catch (Exception e) {
                    allEmailsSent = false; // 이메일 발송 실패 시 플래그 설정
                    e.printStackTrace();
                }
            }

            // 모든 이메일 발송 성공
            if (allEmailsSent) {
                // 오프셋 수동 커밋
                ack.acknowledge();
            } else { // 모든 이메일 발송 실패
                System.out.println("이메일 발송 실패");
            }
        }
    }
}