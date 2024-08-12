package com.sw.journal.journalcrawlerpublisher.repository;

import com.sw.journal.journalcrawlerpublisher.domain.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

// 0809 wildmantle : 클래스명 오타 수정
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
}
