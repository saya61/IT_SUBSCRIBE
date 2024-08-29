package com.sw.journal.journalcrawlerpublisher.crawler.allarticle;

import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.domain.ArticleRank;
import com.sw.journal.journalcrawlerpublisher.domain.Category;
import com.sw.journal.journalcrawlerpublisher.domain.Image;
import com.sw.journal.journalcrawlerpublisher.dto.CrawlingEventDTO;
import com.sw.journal.journalcrawlerpublisher.repository.*;
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

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@SpringBootTest
public class CloudTechNewsAllCrawler {

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
    private static final String[] CATEGORIES = {
            "/categories/cloud-computing-applications/",
            "/categories/cloud-computing-companies/",
            "/categories/cloud-computing-data-analytics/",
            "/categories/cloud-computing-enterprise/",
            "/categories/cloud-computing-industries/",
            "/categories/cloud-computing-infrastructure/",
            "/categories/cloud-computing-iot/",
            "/categories/cloud-computing-platforms/",
            "/categories/cloud-computing-privacy/",
            "/categories/cloud-computing-regulation-government/",
            "/categories/cloud-computing-research/",
            "/categories/cloud-computing-security/"
    };

    @Test
    public void crawlAllNews() throws IOException {
        for (String categoryPath : CATEGORIES) {
            crawlCategory(BASE_URL + categoryPath);
        }
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

            // 기사 중복 검사
            if (articleRepository.findBySource(articleUrl).isPresent()) {
                continue;
            }

            Document articleDocument = Jsoup.connect(articleUrl)
                    .userAgent("Mozilla")
                    .get();

            String dateTimeString = articleDocument.select("time.entry-date.published").attr("datetime");
            OffsetDateTime articleDateTime = OffsetDateTime.parse(dateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            LocalDate articleDate = articleDateTime.toLocalDate();

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
                continue;
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
                continue;
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
        }
    }

    public void createEvent(Category category, Article article) {
        CrawlingEventDTO eventDTO = new CrawlingEventDTO();
        eventDTO.setCategoryId(category.getId());
        eventDTO.setArticleId(article.getId());

        kafkaTemplate.send(CRAWLING_TOPIC, eventDTO);
    }
}
