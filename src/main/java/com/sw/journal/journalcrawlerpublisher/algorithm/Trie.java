package com.sw.journal.journalcrawlerpublisher.algorithm;

import com.sw.journal.journalcrawlerpublisher.domain.Article;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Trie {
    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    // 기사 제목을 Trie 에 추가하는 메서드
    public void insert(String title, Article article) {
        TrieNode node = root;
        title = title.replace(" ", "");
        title = title.toLowerCase();

        for (char c : title.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }
        node.isEndOfWord = true;
        node.articleList.add(article);
    }

    // 키워드를 이용해서 기사 제목 검색
    public List<Article> search(String keyword) {
        keyword = keyword.toLowerCase();
        keyword = keyword.replace(" ", "");

        TrieNode node = root;
        for (char c : keyword.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return new ArrayList<>(); // 키워드가 없는 경우, 빈 리스트 반환
            }
            node = node.children.get(c);
        }
        return collectAllArticles(node);
    }

    // 노드에 해당하는 article 전부 반환하는 메서드
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

class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEndOfWord = false;
    List<Article> articleList = new ArrayList<>();
}

