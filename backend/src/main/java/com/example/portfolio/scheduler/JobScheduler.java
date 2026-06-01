package com.example.portfolio.scheduler;

import com.example.portfolio.service.JobService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobScheduler {
    private final JobService jobService;

    public JobScheduler(JobService jobService) {
        this.jobService = jobService;
    }

    // 매일 새벽 3시에 자동 크롤링 수행
    @Scheduled(cron = "0 0 3 * * *")
    public void runAutomaticCrawling() {
        jobService.crawlJobKoreaDeveloperJobs("개발자", 12);
    }
}
