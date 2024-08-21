package com.sw.journal.journalcrawlerpublisher.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class TimeUtils {

    public static String getRelativeTime(Date createdAt) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdDateTime = LocalDateTime.ofInstant(createdAt.toInstant(), ZoneId.systemDefault());

        Duration duration = Duration.between(createdDateTime, now);

        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();
        long months = (long) (days / 30.5); // 평균적인 달 계산
        long years = days / 365; // 대략적인 계산으로 365일을 한 해로 간주

        if (minutes < 1) {
            return "방금 전";
        } else if (minutes < 60) {
            return minutes + "분 전";
        } else if (hours < 24) {
            return hours + "시간 전";
        } else if (days < 2) {
            return "어제";
        } else if (days < 30.5) { // 30.5일 미만일 경우 일 단위로 표시
            return days + "일 전";
        } else if (months < 12) {
            return months + "개월 전";
        } else {
            return years + "년 전";
        }
    }
}
