package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.domain.ArticleRank;
import com.sw.journal.journalcrawlerpublisher.repository.ArticleRankRepository;
import com.sw.journal.journalcrawlerpublisher.repository.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SpringBootTest
@EnableRedisRepositories
class ArticleRankServiceTest {

    // 기사 랭크의 isActive가 true일 때 사용할 문자열
    private final static String ARTICLE_RANK_KEY = "articleRank";
    // 기사 랭크의 isActive가 false일 때 사용할 문자열
    private final static String EXPIRED_ARTICLE_RANK_KEY = "expiredArticleRank";

    @Autowired
    private ArticleRankRepository articleRankRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private RedisTemplate<String, String> redisObjTemplateDb0;

    @Test
    // 초기 설정
    //@PostConstruct    // Spring 실행 시 실행됨
    public void sendFromDBToRedis(){  // isActive(랭킹 활성)이 true인 값만 Redis에 넣음
        List<ArticleRank> activeArticles = articleRankRepository.findAllByIsActive(true);
        for (ArticleRank articleRank : activeArticles) {
            redisObjTemplateDb0.opsForZSet().add(ARTICLE_RANK_KEY, articleRank.getId().toString(), articleRank.getViews());
        }
    }

    @Test
    // 랭킹 10위 뽑기
    public void getTopTenArticle(){ // Top 10 기사 ID 찾기
        // Redis는 List가 아닌 Set을 반환
        Set<String> topTenArticleIds = redisObjTemplateDb0.opsForZSet().reverseRange(ARTICLE_RANK_KEY, 0, 9);
        // @Test가 사용된 함수는 void 타입이어야 한다. return 사용 불가, 추후 서비스에 넣을 때 return 추가
    }

    @Test
    //@Scheduled(cron = "0 10 4 * * *")   // 매일 04시 10분에 작동됨
    // 1주일 지난 기사 isAcrive를 false로 바꾸기
    public void removeExpiredArticle(){
        List<ArticleRank> isActiveArticles = articleRankRepository.findAllByIsActive(true);     // isActive가 true인 기사 (DB에서는 아직 1주일 지나지 않았다고 알고있는 기사)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");   // 날짜 비교를 위한 format
        LocalDateTime weekAgo = LocalDateTime.parse(LocalDateTime.now().format(formatter), formatter).minusWeeks(1);    // 현재 시간 기준 1주일 전
        for (ArticleRank articleRank : isActiveArticles) {
            Article article = articleRank.getArticle();   // 기사 랭크를 이용해 기사 찾기
            LocalDateTime postDate = LocalDateTime.parse(article.getPostDate().format(formatter), formatter);    // 기사의 작성 날짜
            if (postDate.isBefore(weekAgo)) {   // 1주일 지난 기사
                articleRank.setIsActive(false); // isActive = false
                articleRankRepository.save(articleRank);
                redisObjTemplateDb0.opsForZSet().remove(ARTICLE_RANK_KEY, articleRank.getId().toString());    // Redis의 ARTICLE_RANK_KEY 상위 키(테이블)에서 삭제
            }
        }
    }

    @Test
    public void test(){ // 하단의 increaseArticleCount() 테스트용 코드
        increaseArticleCount(5L);
    }
    // 조회수 증가
    public void increaseArticleCount(Long articleId){
        Optional<Article> ourArticleOptional = articleRepository.findById(articleId);
        if(ourArticleOptional.isPresent()){
            Article article = ourArticleOptional.get();
            Optional<ArticleRank> articleRankOptional = articleRankRepository.findByArticle(article);  // 기사 ID로 기사 조회수 찾기
            if(articleRankOptional.isPresent()){
                ArticleRank articleRank = articleRankOptional.get();
                if(articleRank.getIsActive()){  // 기사가 isActive = true 즉 1주일 넘지 않았다면
                    boolean isExist = redisObjTemplateDb0.opsForZSet().score(ARTICLE_RANK_KEY, articleId.toString()) != null; // Redis에 이미 ARTICLE_RANK_KEY로 있는 기사이면 ture
                    // 하단의 기능을 크롤링 할 때 넣을지 지금처럼 사용자가 클릭했을 때 넣을지 추후 확인
                    if(!isExist){   // 없으면 ARTICLE_RANK_KEY로 넣어준다
                        redisObjTemplateDb0.opsForZSet().add(ARTICLE_RANK_KEY, articleRank.getId().toString(), articleRank.getViews());
                    }
                    redisObjTemplateDb0.opsForZSet().incrementScore(ARTICLE_RANK_KEY, articleRank.getId().toString(), 1);
                }
                else {  // 기사가 isActive = false 즉 1주일 넘었다면
                    boolean isExist = redisObjTemplateDb0.opsForZSet().score(EXPIRED_ARTICLE_RANK_KEY, articleId.toString()) != null; // Redis에 이미 EXPIRED_ARTICLE_RANK_KEY 있는 기사이면 ture
                    if(!isExist){   // 없으면 EXPIRED_ARTICLE_RANK_KEY로 넣어준다
                        redisObjTemplateDb0.opsForZSet().add(EXPIRED_ARTICLE_RANK_KEY, articleRank.getId().toString(), articleRank.getViews());
                    }
                    redisObjTemplateDb0.opsForZSet().incrementScore(EXPIRED_ARTICLE_RANK_KEY, articleRank.getId().toString(), 1);
                }
            }
        }
    }

    @Test
    // 1시간 마다 Redis -> DB로 전송
    //@Scheduled(cron = "0 30 * * * ?")    // 매 시간 30분에 실행됨
    public void updateFromRedisToDB(){  // isActive 상관 없이 모든 값 업데이트
        // Redis에서 isActive가 true인 모든 데이터 가져옴
        Set<String> activeArticleIds = redisObjTemplateDb0.opsForZSet().reverseRange(ARTICLE_RANK_KEY, 0, -1);
        if (activeArticleIds != null) {
            for (String activeArticleId : activeArticleIds) {
                Long count = redisObjTemplateDb0.opsForZSet().score(ARTICLE_RANK_KEY, activeArticleId).longValue();
                Optional<ArticleRank> articleRankOptional = articleRankRepository.findById(Long.parseLong(activeArticleId));
                if(articleRankOptional.isPresent()){
                    ArticleRank articleRank = articleRankOptional.get();
                    articleRank.setViews(count);
                    articleRankRepository.save(articleRank);
                }
            }
        }
        // Redis에서 isActive가 false인 모든 데이터 가져옴
        Set<String> nonActiveArticleIds = redisObjTemplateDb0.opsForZSet().reverseRange(EXPIRED_ARTICLE_RANK_KEY, 0, -1);
        if (!nonActiveArticleIds.isEmpty()) {  // isActive가 false인 데이터가 있을 경우 실행
            // nonActiveArticleIds != null 형식은 nonActiveArticleIds에 값이 없어도 [](빈 배열)로 반환되어 true로 인식함
            for (String nonActiveArticleId : nonActiveArticleIds) {
                Long count = redisObjTemplateDb0.opsForZSet().score(EXPIRED_ARTICLE_RANK_KEY, nonActiveArticleId).longValue();
                Optional<ArticleRank> articleRankOptional = articleRankRepository.findById(Long.parseLong(nonActiveArticleId));
                if(articleRankOptional.isPresent()){
                    ArticleRank articleRank = articleRankOptional.get();
                    articleRank.setViews(count);
                    articleRankRepository.save(articleRank);
                }
            }
            // isActive가 false인 모든 데이터 Redis에서 삭제
            redisObjTemplateDb0.delete(EXPIRED_ARTICLE_RANK_KEY);
        }
    }
}