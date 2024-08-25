package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.*;
import com.sw.journal.journalcrawlerpublisher.repository.ArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.CategoryRepository;
import com.sw.journal.journalcrawlerpublisher.repository.TagRepository;
import com.sw.journal.journalcrawlerpublisher.repository.UserFavoriteCategoryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// wild-mantle 2024-07-08
@Getter @Setter
@RequiredArgsConstructor
@Service
public class RecommendArticleService {
    // 필드 주입에서 생성자 주입으로 변경
    private final ArticleRepository articleRepository;

    private final CategoryRepository categoryRepository;

    private final TagRepository tagRepository;

    private final UserFavoriteCategoryRepository userFavoriteCategoryRepository;

    private final ArticleService articleService;
    private final BookmarkService bookmarkService;

    // 최신 기사를 주어진 개수만큼 조회
    public List<Article> findRecentArticles(int limit) {
        // PageRequest.of(0, limit)을 통해 첫 번째 페이지에서 주어진 개수만큼 기사 조회
        // 조회된 결과에서 기사 리스트 반환
        return articleRepository.findAll(PageRequest.of(0, limit)).getContent();
    }

    // 사용자 선호 카테고리의 기사 조회
    public List<Article> findByUserFavoriteCategories(Member member) {
        // 사용자 선호 카테고리 리스트 조회
        List<UserFavoriteCategory> favoriteCategories = userFavoriteCategoryRepository.findByMember(member);
        // 선호 카테고리 리스트에서 카테고리 객체만 추출하여 리스트로 변환
        List<Category> categories = favoriteCategories.stream()
                .map(UserFavoriteCategory::getCategory) // UserFavoriteCategory에서 Category 객체 추출
                .collect(Collectors.toList()); // 추출한 Category 객체를 리스트로 수집
        // 해당 카테고리에 속한 기사를 조회하여 반환
        return articleRepository.findByCategories(categories);
    }

    // 사용자 선호 카테고리의 기사 중에서 최신 기사를 주어진 개수만큼 조회
    public List<Article> findTopByUserFavoriteCategoriesOrderByPostDateDesc(Member member, int limit) {
        // 사용자 선호 카테고리 리스트 조회
        List<UserFavoriteCategory> favoriteCategories = userFavoriteCategoryRepository.findByMember(member);
        // 선호 카테고리 리스트에서 카테고리 객체만 추출하여 리스트로 변환
        List<Category> categories = favoriteCategories.stream()
                .map(UserFavoriteCategory::getCategory) // UserFavoriteCategory에서 Category 객체 추출
                .collect(Collectors.toList()); // 추출한 Category 객체를 리스트로 수집
        // 해당 카테고리에 속한 기사 중 최신 기사를 주어진 개수만큼 조회하여 반환
        return articleRepository.findTopByCategoriesOrderByPostDate(categories, PageRequest.of(0, limit));
    }
    public List<RecommendedItem> recommendArticlesForUser(long userId, int maxResults) throws Exception {
        // 데이터 모델 생성
        DataModel dataModel = bookmarkService.getMahoutDataModel();

        // 유사도 계산
        UserSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);

        // 이웃 선정
        UserNeighborhood neighborhood = new NearestNUserNeighborhood(2, similarity, dataModel);

        // 추천 엔진 생성
        Recommender recommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity);

        // 추천 결과 생성
        return recommender.recommend(userId, maxResults);
    }





}