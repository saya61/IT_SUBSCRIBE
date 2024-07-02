package com.sw.journal.journalcrawlerpublisher.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

@Service
public class MainPageService {
    public String getMainPage() {
        return "index";
    }
}
