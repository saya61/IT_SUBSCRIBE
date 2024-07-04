package com.sw.journal.journalcrawlerpublisher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sw.journal.journalcrawlerpublisher.domain.*;

public interface MemberRepository extends JpaRepository<Member, Long> {}
