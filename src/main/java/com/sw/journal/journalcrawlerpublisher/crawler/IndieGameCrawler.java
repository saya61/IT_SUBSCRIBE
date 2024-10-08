package com.sw.journal.journalcrawlerpublisher.crawler;

import com.sw.journal.journalcrawlerpublisher.domain.*;
import com.sw.journal.journalcrawlerpublisher.dto.CrawlingEventDTO;
import com.sw.journal.journalcrawlerpublisher.repository.*;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
class IndieGameCrawler {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final ArticleRankRepository articleRankRepository;
    private final KafkaTemplate<String, CrawlingEventDTO> kafkaTemplate;
    // 크롤링 이벤트를 발행할 Kafka 토픽
    @Value("${spring.kafka.template.default-topic}")
    private String CRAWLING_TOPIC;

    public boolean crawlArticles(String articleUrl){
        Connection conn = Jsoup.connect(articleUrl);
        try {
            Document doc = conn.get();
            Elements elem = doc.select("div.main.ts-contain.cf.right-sidebar"); //set 자료형으로 관리해도 됨

            // 기사 제목
            String articleTitle = elem.select("div.the-post-header>div.post-meta>h1").text();

            // 기사 이미지 (이미지 태그가 두가지 케이스로 나뉨)
            // 첫번째 케이스
            String imgUrl = elem.select("figure.wp-block-image>img").attr("src");
            // 두번째 케이스
            if(imgUrl.isEmpty()){
                imgUrl = elem.select("div.wp-block-image>figure>img").attr("src");
            }

            // 기사 내용
            String articleContent = elem.select("div.ts-row div.post-content").text();

            // 1. 기사 중복 검사
            //assert ourArticleRepository.findBySource(articleUrl) == null;   // 서비스에서는 쓰지 않는게 좋다
            // 서비스에서는 junit을 사용하지 않는다
            // return null로 반환하고 null을 받는곳을 만든다
            // count로 중복 값이 들어오면 저장해서 insite를 만든다
            if(articleRepository.findBySource(articleUrl).isPresent()){
                return false;
            }

            // 2. 카테고리 저장
            Optional<Category> categoryOptional = categoryRepository.findByName("게임 개발");
            Category category = null;
            // 카테고리가 DB에 존재할 경우
            if (categoryOptional.isPresent()) {
                category = categoryOptional.get();
            }
            // 카테고리가 DB에 존재하지 않을 경우
            else {
                System.out.println("카테고리를 찾을 수 없습니다.");
            }

            // 3. 기사 객체 생성 및 저장
            Article article = new Article();
            article.setSource(articleUrl);   // 기사 Url 저장
            article.setCategory(category);   // 기사 카테고리
            article.setTitle(articleTitle);  // 기사 제목
            article.setContent(articleContent);  // 기사 내용
            article.setPostDate(LocalDateTime.now());  // 게시 날짜
            Article savedArticle;
            try {
                savedArticle = articleRepository.save(article);
            } catch (DataIntegrityViolationException ex) {
                System.out.println(ex.getMessage());
                return false;
            }

            // 4. 이미지 저장
            // 이미지가 없는 기사도 있음
            if(!imgUrl.isEmpty()) {
                Image image = new Image();
                image.setImgUrl(imgUrl);
                image.setArticle(savedArticle);
                imageRepository.save(image);
            }

//            이 뉴스 사이트에는 태그가 없어서 태그 저장 코드 주석 처리
//            // 5-1. 태그 각각 저장
//            for(Element e : doc.select("div.the-post s-post-large>article>div.the-post-tags")) {
//                String tagName = e.select("a").text();
//                if(tagRepository.findByName(tagName).isEmpty()) {
//                    Tag tag = new Tag();
//                    tag.setName(tagName);
//                    try {
//                        tagRepository.save(tag);
//                    } catch (DataIntegrityViolationException ex) {
//                        System.out.println(ex.getMessage());
//                        return false;
//                    }
//                }
//                // 5-2. 태그와 기사 중계 테이블 저장
//                TagArticle tagArticle = new TagArticle();
//                tagArticle.setArticle(savedArticle);
//                tagArticle.setTag(tagRepository.findByName(tagName).get()); // 위에서 유무 검사 후 저장된 값 가져오기
//                tagArticleRepository.save(tagArticle);
//            }

            // 6. 기사 랭크(조회수) 저장
            if(articleRankRepository.findByArticle(savedArticle).isEmpty()) {
                Random random = new Random();
                ArticleRank articleRank = new ArticleRank();
                articleRank.setArticle(savedArticle);
                articleRank.setViews((long) random.nextInt(100));   // 조회수에 관한 데이터를 랜덤으로 넣음 (추후 삭제)
                articleRank.setIsActive(true);
                try {
                    articleRankRepository.save(articleRank);
                } catch (DataIntegrityViolationException ex) {
                    System.out.println(ex.getMessage());
                    return false;
                }
            }

            // 7. 크롤링 이벤트 생성
            createEvent(category, savedArticle);

            return true;

        } catch (IOException e) {
            System.err.println("페이지 요청 중 에러 발생");
            e.printStackTrace();
            return false;
        }
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void crawlingCio(){
        String URL = "https://indiegame.com/archives/category/news";
        String compareURL = "https://indiegame.com/archives/";  // 문자 0~30
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1);

        Connection conn = Jsoup.connect(URL);
        try {
            Document doc = conn.get();
            Elements elems = doc.select("div.main>div.ts-row div.loop.loop-grid.loop-grid-base.grid.grid-2 article"); //set 자료형으로 관리해도 됨
            for (Element elem : elems) {
                String link = elem.select("h2.is-title>a").attr("href");   // 기사의 링크
                String pubDate = elem.select("div.post-meta-items>span.meta-item>span.date-link>time").attr("datetime"); // 기사 발행 시간
                // ZonedDateTime으로 파싱
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(pubDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                // LocalDateTime으로 변환
                LocalDateTime articleLocalDateTime = zonedDateTime.toLocalDateTime();

                // 시간 비교
                if(articleLocalDateTime.isBefore(today) && articleLocalDateTime.isAfter(yesterday)) {
                    // 크롤링 가능한 기간
                    // rss에서 크롤링을 수행할 때 원하지 않는 url을 수행하지 않기 위함
                    // 저장 유무를 전달받음 이후 count로 중복 기사가 몇번 발생했는지 저장하여 insite를 만듬 로그 파일 만들기 logforj
                    if(compareURL.regionMatches(0, link, 0, 31)) {
                        boolean saved = crawlArticles(link);
                    }
                }
                else {
                    System.out.println("날짜가 지났습니다.");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("페이지 요청 중 에러 발생");
            e.printStackTrace();
        }

    }

    // 크롤링 이벤트 발생 메서드
    // 크롤링 이벤트 발생 -> Kafka 로 발행
    public void createEvent(Category category, Article article) {
        // 이벤트 DTO 생성 및 필드 설정
        CrawlingEventDTO eventDTO = new CrawlingEventDTO();
        eventDTO.setCategoryId(category.getId()); // 신작 기사 카테고리 id
        eventDTO.setArticleId(article.getId()); // 신작 기사 id

        // Kafka로 이벤트 발행
        kafkaTemplate.send(CRAWLING_TOPIC, eventDTO);
    }
}