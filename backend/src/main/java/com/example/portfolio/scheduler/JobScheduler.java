package com.example.portfolio.scheduler;

import com.example.portfolio.service.JobService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobKoreaAutoCrawler {
    private final JobService jobService;

    public JobKoreaAutoCrawler(JobService jobService) {
        this.jobService = jobService;
    }

    @Scheduled(initialDelayString = "${app.crawler.initial-delay-ms:15000}", fixedDelayString = "${app.crawler.fixed-delay-ms:600000}")
    public void refreshDeveloperJobs() {
        jobService.runAutomaticJobKoreaCrawl();
    }
}
