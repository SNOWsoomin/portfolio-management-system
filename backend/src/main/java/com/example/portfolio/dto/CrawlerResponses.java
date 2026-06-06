package com.example.portfolio.dto;

import com.example.portfolio.dto.Responses.JobPostResponse;
import java.time.LocalDateTime;
import java.util.List;

public class CrawlerResponses {
    public record JobCrawlerResponse(
            String sourceName,
            String keyword,
            int requestedLimit,
            int fetchedCount,
            int savedCount,
            LocalDateTime crawledAt,
            List<JobPostResponse> jobs
    ) {
    }

    public record JobCrawlerStatusResponse(
            String sourceName,
            String keyword,
            int requestedLimit,
            int fetchedCount,
            int savedCount,
            int totalJobCount,
            LocalDateTime lastCrawledAt,
            boolean running,
            String lastError
    ) {
    }
}
