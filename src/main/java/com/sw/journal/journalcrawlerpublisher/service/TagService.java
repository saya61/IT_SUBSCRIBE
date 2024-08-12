package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.domain.Tag;
import com.sw.journal.journalcrawlerpublisher.domain.TagArticle;
import com.sw.journal.journalcrawlerpublisher.repository.OurArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.TagArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final TagArticleRepository tagArticleRepository;
    private final OurArticleRepository ourArticleRepository;

    // 기사에 연결된 태그들을 조회
    public List<Tag> findByArticle(Article article) {
        // 기사에 연결된 TagArticle 리스트 조회
        List<TagArticle> tagArticleList = tagArticleRepository.findByArticle(article);
        return tagArticleList.stream()
                .map(TagArticle::getTag)
                .collect(Collectors.toList());
    }

    public Map<Long, List<Tag>> findTagsByArticleIds(List<Long> articleIds) {
        List<TagArticle> tagArticles = tagArticleRepository.findByArticleIds(articleIds);

        return tagArticles.stream()
                .collect(Collectors.groupingBy(
                        tagArticle -> tagArticle.getArticle().getId(),
                        Collectors.mapping(TagArticle::getTag, Collectors.toList())
                ));
    }
}
