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
    ) {}
}
