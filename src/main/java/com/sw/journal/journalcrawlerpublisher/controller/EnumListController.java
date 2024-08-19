package com.sw.journal.journalcrawlerpublisher.controller;

import com.sw.journal.journalcrawlerpublisher.constant.ReportReason;
import com.sw.journal.journalcrawlerpublisher.constant.CommentReportStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// DTO 를 적용 안하고 구현
@RestController
@RequestMapping("/enum-list")
public class EnumListController {

    // ReportReason Enum 을 반환함
    @GetMapping("/comment-report-reasons")
    public List<Map<String, String>> getReportReasons() {
        return Arrays.stream(ReportReason.values())
                .map(reason -> Map.of(
                        "name", reason.name(),  // name 값은 미반환으로 설정
                        "description", reason.getDescription()
                ))
                .collect(Collectors.toList());
    }

    // CommentReportStatus Enum 을 반환함
    @GetMapping("/comment-statuses")
    public List<Map<String, String>> getStatuses() {
        return Arrays.stream(CommentReportStatus.values())
                .map(status -> Map.of(
                        "name", status.name(),  // name 값은 미반환으로 설정
                        "description", status.getDescription()
                ))
                .collect(Collectors.toList());
    }
}