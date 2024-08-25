package com.sw.journal.journalcrawlerpublisher.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

public class BanDTO {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private Long userId;
        private int days;
        private Long reportId;
        private String reason;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Long banId;
        private String username;
        private Long reportId;
        private LocalDate banStartDate;
        private LocalDate banEndDate;
        private String reason;
        private boolean active;
    }

}
