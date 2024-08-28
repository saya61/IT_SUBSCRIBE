package com.sw.journal.journalcrawlerpublisher.crawler;

import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.domain.ArticleRank;
import com.sw.journal.journalcrawlerpublisher.domain.Category;
import com.sw.journal.journalcrawlerpublisher.domain.Image;
import com.sw.journal.journalcrawlerpublisher.dto.CrawlingEventDTO;
import com.sw.journal.journalcrawlerpublisher.repository.ArticleRankRepository;
import com.sw.journal.journalcrawlerpublisher.repository.ArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.CategoryRepository;
import com.sw.journal.journalcrawlerpublisher.repository.ImageRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootTest
public class CloudTechNewsCrawlerTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ArticleRankRepository articleRankRepository;

    @Autowired
    private KafkaTemplate<String, CrawlingEventDTO> kafkaTemplate;

    @Value("${spring.kafka.template.default-topic}")
    private String CRAWLING_TOPIC;

    private static final String BASE_URL = "https://www.cloudcomputing-news.net";
    String url = BASE_URL + "/cloud-tech-news/";

    // 멀티스레딩을 위한 스레드 풀 (최대 10개의 스레드를 사용)
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    // 데이터 동기화를 위한 ReentrantLock
    private final ReentrantLock lock = new ReentrantLock();

//    @Scheduled(cron = "0 0 4 * * *") // 새벽 4시에 실행
    @Test
    public void crawlAllNews() throws IOException {
        crawlCategory(url);
    }

    private void crawlCategory(String url) throws IOException {
        Document document = Jsoup.connect(url)
                .userAgent("Mozilla")
                .get();

        Elements articleHeaders = document.select("div.grid-x.grid-margin-x header.article-header");

        for (Element articleHeader : articleHeaders) {
            String articleUrl = articleHeader.select("a").attr("href");

            if (!articleUrl.startsWith("http")) {
                articleUrl = BASE_URL + articleUrl;
            }

            // 각 기사를 비동기적으로 크롤링
            String finalArticleUrl = articleUrl;
            executorService.submit(() -> {
                lock.lock(); // 스레드 간 동기화
                try {
                    processArticle(finalArticleUrl);
                } finally {
                    lock.unlock(); // 작업 완료 후 락 해제
                }
            });
        }

        executorService.shutdown(); // 작업이 끝나면 스레드풀 종료
    }

    private void processArticle(String articleUrl) {
        try {
            // 기사 중복 검사
            if (articleRepository.findBySource(articleUrl).isPresent()) {
                return;
            }

            Document articleDocument = Jsoup.connect(articleUrl)
                    .userAgent("Mozilla")
                    .get();

            // 게시 날짜 추출 및 파싱
            String dateTimeString = articleDocument.select("time.entry-date.published").attr("datetime");
            OffsetDateTime articleDateTime = OffsetDateTime.parse(dateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            LocalDate articleDate = articleDateTime.toLocalDate();

            // 3일이 지난 기사인지 확인
            LocalDate threeDaysAgo = LocalDate.now().minusDays(3);
            if (articleDate.isBefore(threeDaysAgo)) {
                System.out.println("3일이 지난 기사가 감지되어 크롤링을 중단합니다: " + articleUrl);
                return;
            }

            String title = articleDocument.select("h1.entry-title.single-title").text();
            String imageUrl = articleDocument.select("img.wp-post-image").attr("src");

            Elements paragraphs = articleDocument.select("div.cell.small-12.medium-12.large-12 p");
            StringBuilder contentBuilder = new StringBuilder();
            for (Element paragraph : paragraphs) {
                contentBuilder.append(paragraph.text()).append("\n");
            }
            String content = contentBuilder.toString().trim();

            String unwantedStart = "Want to learn more about cybersecurity and the cloud from industry leaders?";
            int index = content.indexOf(unwantedStart);
            if (index != -1) {
                content = content.substring(0, index).trim();
            }

            String seeAlso = "See also:";
            int seeAlsoIndex = content.lastIndexOf(seeAlso);
            if (seeAlsoIndex != -1) {
                content = content.substring(0, seeAlsoIndex).trim();
            }

            Optional<Category> categoryOptional = categoryRepository.findByName("클라우드");

            if (!categoryOptional.isPresent()) {
                System.out.println("카테고리를 찾을 수 없습니다.");
                return;
            }

            Category category = categoryOptional.get();

            Article article = new Article();
            article.setSource(articleUrl);
            article.setCategory(category);
            article.setTitle(title);
            article.setContent(content);
            article.setPostDate(LocalDateTime.now());

            Article savedArticle;

            try {
                savedArticle = articleRepository.save(article);
            } catch (DataIntegrityViolationException ex) {
                System.out.println(ex.getMessage());
                return;
            }

            if (!imageUrl.isEmpty()) {
                Image image = new Image();
                image.setImgUrl(imageUrl);
                image.setArticle(savedArticle);
                imageRepository.save(image);
            }

            if (articleRankRepository.findByArticle(savedArticle).isEmpty()) {
                ArticleRank articleRank = new ArticleRank();
                articleRank.setArticle(savedArticle);
                articleRank.setViews(0L);
                articleRank.setIsActive(true);
                articleRankRepository.save(articleRank);
            }

            // Kafka 이벤트 발행
            createEvent(category, savedArticle);

        } catch (IOException e) {
            System.err.println("기사 처리 중 에러 발생: " + articleUrl);
            e.printStackTrace();
        }
    }

    public void createEvent(Category category, Article article) {
        CrawlingEventDTO eventDTO = new CrawlingEventDTO();
        eventDTO.setCategoryId(category.getId());
        eventDTO.setArticleId(article.getId());

        kafkaTemplate.send(CRAWLING_TOPIC, eventDTO);
    }
}
