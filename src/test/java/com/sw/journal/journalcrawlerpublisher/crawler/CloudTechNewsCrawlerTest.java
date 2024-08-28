package com.sw.journal.journalcrawlerpublisher.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootTest
public class CloudTechNewsCrawlerTest {
    private static final String BASE_URL = "https://www.cloudcomputing-news.net";

    @Test
    public void crawlNews() throws IOException {
        String url = BASE_URL + "/cloud-tech-news/";
        // User-Agent를 설정하여 서버의 차단을 피함
        Document document = Jsoup.connect(url)
                .userAgent("Mozilla")
                .get();

        // 첫 번째 단계: 메인 페이지에서 기사 링크들을 가져옴
        Elements articleHeaders = document.select("div.grid-x.grid-margin-x header.article-header");

        for (Element articleHeader : articleHeaders) {
            String articleUrl = articleHeader.select("a").attr("href");

            // articleUrl이 상대 경로라면 절대 경로로 변환
            if (!articleUrl.startsWith("http")) {
                articleUrl = BASE_URL + articleUrl;
            }
            System.out.println("기사 링크: " + articleUrl);

            // 기사 페이지로 이동하여 필요한 요소들을 추출
            Document articleDocument = Jsoup.connect(articleUrl)
                    .userAgent("Mozilla")
                    .get();

            // 게시 날짜 추출 및 파싱
            String dateTimeString = articleDocument.select("time.entry-date.published").attr("datetime");
            OffsetDateTime articleDateTime = OffsetDateTime.parse(dateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            LocalDate articleDate = articleDateTime.toLocalDate();

            LocalDate twoWeeksAgo = LocalDate.now().minusWeeks(2);

            // 날짜 필터링: 2주 이내의 기사만 처리
            if (articleDate.isAfter(twoWeeksAgo) || articleDate.isEqual(twoWeeksAgo)) {
                // 제목 추출
                String title = articleDocument.select("h1.entry-title.single-title").text();
                System.out.println("기사 제목: " + title);

                // 이미지 URL 추출
                String imageUrl = articleDocument.select("img.wp-post-image").attr("src");
                System.out.println("이미지 URL: " + imageUrl);

                // 특정 클래스의 p 태그들만 선택
                Elements paragraphs = articleDocument.select("div.cell.small-12.medium-12.large-12 p");
                StringBuilder contentBuilder = new StringBuilder();
                for (Element paragraph : paragraphs) {
                    contentBuilder.append(paragraph.text()).append("\n");
                }
                String content = contentBuilder.toString().trim();
//                // 불필요한 텍스트 제거
//                String unwantedStart = "Want to learn more about cybersecurity and the cloud from industry leaders?";
//                int index = content.indexOf(unwantedStart);
//                if (index != -1) {
//                    content = content.substring(0, index).trim();
//                }
                System.out.println("기사 내용: " + content);

                // 각 기사에 대해 필요한 정보를 처리하거나 저장하는 로직 추가
            } else {
                System.out.println("이 기사는 2주 이상 되었습니다: " + articleUrl);
            }
        }
    }
}
