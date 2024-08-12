package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.OurArticle;
import com.sw.journal.journalcrawlerpublisher.domain.Tag;
import com.sw.journal.journalcrawlerpublisher.domain.TagArticle;
import com.sw.journal.journalcrawlerpublisher.repository.OurArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.TagArticleRepository;
import com.sw.journal.journalcrawlerpublisher.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {
    // 0809 wildmantle : 안쓰이는 변수 제거
    private final TagArticleRepository tagArticleRepository;

    // 기사에 연결된 태그들을 조회
    public List<Tag> findByArticle(OurArticle article) {
        // 기사에 연결된 TagArticle 리스트 조회
        List<TagArticle> tagArticleList = tagArticleRepository.findByArticle(article);

        // tagArticleList 의 각 태그를 Tag 객체로 변환 후 리스트로 반환
        return tagArticleList.stream() // TagArticle 리스트를 스트림으로 변환
                .map(TagArticle::getTag) // 각 TagArticle 객체에서 Tag 객체 추출
                .collect(Collectors.toList()); // 추출한 Tag 객체들을 리스트로 수집
    }
}
