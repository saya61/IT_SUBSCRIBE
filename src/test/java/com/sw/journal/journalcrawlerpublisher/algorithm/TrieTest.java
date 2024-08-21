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

class Trie {
    private final TrieNode root;

    public TrieNode getRoot(){
        return this.root;
    }

    public Trie() {
        root = new TrieNode();
    }

    // 기사 제목을 Trie에 추가하는 메서드
    public void insert(String title, Article article) {
        TrieNode node = root;
        title = title.replace(" ","");
        title = title.toLowerCase();

        for (char c : title.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }
        node.isEndOfWord = true;
        node.articleList.add(article);
    }

    // 키워드를 이용해서
    public List<Article> search(String keyword) {
        keyword = keyword.toLowerCase();
        keyword = keyword.replace(" ","");

        TrieNode node = root;
        for (char c : keyword.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return new ArrayList<>(); // 키워드가 없는 경우, 빈 리스트 반환
            }
            node = node.children.get(c);
        }
        return collectAllArticles(node);
    }

    public List<Article> collectAllArticles(TrieNode node) {
        List<Article> result = new ArrayList<>();
        if (node.isEndOfWord) {
            result.addAll(node.articleList);
        }
        for (TrieNode childNode : node.children.values()) {
            result.addAll(collectAllArticles(childNode));
        }
        return result;
    }

}

class TrieNode{
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEndOfWord = false;
    List<Article> articleList = new ArrayList<>();
}

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
        List<Article>result = trieArticle.search("글로벌");
        System.out.println(result);
    }
}