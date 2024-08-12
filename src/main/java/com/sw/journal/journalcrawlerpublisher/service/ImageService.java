package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.Image;
import com.sw.journal.journalcrawlerpublisher.domain.Article;
import com.sw.journal.journalcrawlerpublisher.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;

    // 기사 이미지 리스트 조회
    public List<Image> findByArticle(Article article) {
        return imageRepository.findByOurArticle(article);
    }

    public Map<Long, List<Image>> findImagesByArticleIds(List<Long> articleIds) {
        List<Image> images = imageRepository.findByArticleIds(articleIds);

        return images.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        image -> image.getArticle().getId(),
                        java.util.stream.Collectors.toList()
                ));
    }
}