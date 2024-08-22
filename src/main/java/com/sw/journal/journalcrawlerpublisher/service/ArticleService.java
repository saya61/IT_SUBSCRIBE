package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.algorithm.Trie;
import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.domain.Category;
import com.sw.journal.journalcrawlerpublisher.domain.Tag;
import com.sw.journal.journalcrawlerpublisher.repository.ArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.CategoryRepository;
import com.sw.journal.journalcrawlerpublisher.repository.TagArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.TagRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@RequiredArgsConstructor
@Service
public class ArticleService {

    // 필드 주입에서 생성자 주입으로 변경
    private final ArticleRepository articleRepository;

    private final CategoryRepository categoryRepository;

    private final TagRepository tagRepository;

    private final TagArticleRepository tagArticleRepository;

    private Trie trieArticle = new Trie();

    public Optional<Article> findById(Long id) {
        return articleRepository.findById(id);
    }

    // 카테고리별 페이지네이션된 기사 검색
    public Page<Article> findByCategory(Category category, Pageable pageable) {
        return articleRepository.findByCategory(category, pageable);
    }

    public Page<Article> findAll(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }
    public List<Article> findAll() { return articleRepository.findAll(); }

    // 1개의 카테고리로만 검색
    public List<Article> findByCategory(Category category) {
        return articleRepository.findByCategory(category);
    }


    // n개의 카테고리로 검색
    public List<Article> findByCategories(List<Category> categories) {
        return articleRepository.findByCategories(categories);
    }

    // 1개 태그로만 검색
    public List<Article> findByTag(Tag tag) {
        return articleRepository.findByTag(tag);
    }

    // n개 태그로 검색
    public List<Article> findByTags(List<Tag> tags) {
        return articleRepository.findByTags(tags, tags.size());
    }

    // 1개 카테고리, 1개 태그로 검색
    public List<Article> findByCategoryAndTag(Category category, Tag tag) {
        return articleRepository.findByCategoryAndTag(category, tag);
    }

    // 1개 카테고리, n개 태그로 검색
    public List<Article> findByCategoryAndTags(Category category, List<Tag> tags) {
        return articleRepository.findByCategoryAndTags(category, tags, tags.size());
    }

    // n개 카테고리, 1개 태그로 검색
    public List<Article> findByCategoriesAndTag(List<Category> categories, Tag tag) {
        return articleRepository.findByCategoriesAndTag(categories, tag);
    }

    // n개 카테고리, n개 태그로 검색
    public List<Article> findByCategoriesAndTags(List<Category> categories, List<Tag> tags) {
        return articleRepository.findByCategoriesAndTags(categories, tags, tags.size());
    }

    // @PostConstruct : 의존성 주입이 끝나면 한번만 실행시킴.
    @PostConstruct
    public void initializeTrie(){
        List<Article> allArticles = articleRepository.findAll();
        for (Article article : allArticles) {
            trieArticle.insert(article.getTitle(), article);
        }
    }


    public List<Article> searchArticle(String keyWords){
        return trieArticle.search(keyWords);
    }

    public Page<Article> getTrieArticles(Pageable pageable) {
        List<Article> allArticles = trieArticle.collectAllArticles(trieArticle.getRoot());

        // 페이징을 처리하기 위해 시작과 끝 인덱스를 계산합니다.
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allArticles.size());

        // 페이지의 데이터 부분을 서브리스트로 가져옵니다.
        List<Article> pagedArticles = allArticles.subList(start, end);

        // PageImpl을 사용하여 Page<Article> 객체를 생성합니다.
        return new PageImpl<>(pagedArticles, pageable, allArticles.size());
    }

}
