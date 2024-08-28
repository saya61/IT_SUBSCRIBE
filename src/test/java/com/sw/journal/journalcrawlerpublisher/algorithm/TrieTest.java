package com.sw.journal.journalcrawlerpublisher.algorithm;

import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.service.ArticleService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class TrieTest{
    Trie trieArticle = new Trie();
    @Autowired
    private ArticleService articleService;

    @Test
    public void initializeTrie(){
        List<Article> allArticles = articleService.findAll();
        for (Article article : allArticles) {
            trieArticle.insert(article.getTitle(), article);
        }
    }


    @Test
    public void showTrieArticles(){
        List<Article> allArticle = trieArticle.collectAllArticles(trieArticle.getRoot());
        for (Article article : allArticle) {
            System.out.println(article.getTitle());
        }
    }

    @Test
    public void initializeAndShowTrieArticle(){
        initializeTrie();
        showTrieArticles();
    }

    @Test
    @Transactional
    public void searchTrieArticle(){
        initializeTrie();
        List<Article>result = trieArticle.search("칼럼");
        System.out.println(result);
    }
}