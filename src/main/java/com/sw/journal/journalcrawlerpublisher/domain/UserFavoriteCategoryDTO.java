package com.sw.journal.journalcrawlerpublisher.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@RequiredArgsConstructor
public class UserFavoriteCategoryDTO { // DTO 클래스 역할을 하는 클래스
    private List<Long> categoryIds;
}
