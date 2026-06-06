package com.example.portfolio.controller;

import com.example.portfolio.dto.ApiResponse;
import com.example.portfolio.dto.CrawlerResponses.JobCrawlerStatusResponse;
import com.example.portfolio.dto.Responses.JobPostResponse;
import com.example.portfolio.dto.Responses.MatchResponse;
import com.example.portfolio.service.CurrentUserService;
import com.example.portfolio.service.JobService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService jobService;
    private final CurrentUserService currentUserService;

    public JobController(JobService jobService, CurrentUserService currentUserService) {
        this.jobService = jobService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ApiResponse<List<JobPostResponse>> jobs() {
        return ApiResponse.ok(jobService.getAll());
    }

    @GetMapping("/crawl-status")
    public ApiResponse<JobCrawlerStatusResponse> crawlerStatus() {
        return ApiResponse.ok(jobService.getCrawlerStatus());
    }

    @GetMapping("/{id}")
    public ApiResponse<JobPostResponse> one(@PathVariable Long id) {
        return ApiResponse.ok(jobService.getOne(id));
    }

    @GetMapping("/{id}/match")
    public ApiResponse<MatchResponse> match(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok(jobService.match(currentUserService.getCurrentUser(authentication), id));
    }

    @GetMapping("/matches")
    public ApiResponse<List<MatchResponse>> matches(Authentication authentication) {
        return ApiResponse.ok(jobService.matchAll(currentUserService.getCurrentUser(authentication)));
    }
}
