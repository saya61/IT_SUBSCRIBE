package com.sw.journal.journalcrawlerpublisher.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@RequiredArgsConstructor
// 사용자 선호 카테고리 편집 데이터를 전송하기 위한 DTO
public class UserFavoriteCategoryDTO {
    private List<Long> categoryIds;
}
