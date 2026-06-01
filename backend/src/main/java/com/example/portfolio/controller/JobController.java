package com.example.portfolio.controller;

import com.example.portfolio.dto.ApiResponse;
import com.example.portfolio.dto.Responses.JobPostResponse;
import com.example.portfolio.dto.Responses.MatchResponse;
import com.example.portfolio.dto.CrawledJobPost; // DTO import 추가
import com.example.portfolio.service.CurrentUserService;
import com.example.portfolio.service.JobService;
import com.example.portfolio.service.JobKoreaCrawlerService; // 서비스 import 추가
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*; // GetMapping 등 포함

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService jobService;
    private final CurrentUserService currentUserService;
    private final JobKoreaCrawlerService jobKoreaCrawlerService; // 필드 추가

    // 생성자 주입
    public JobController(JobService jobService, 
                         CurrentUserService currentUserService, 
                         JobKoreaCrawlerService jobKoreaCrawlerService) {
        this.jobService = jobService;
        this.currentUserService = currentUserService;
        this.jobKoreaCrawlerService = jobKoreaCrawlerService;
    }

    // [새로 추가] 크롤링 기능 호출 API
    @GetMapping("/crawl")
    public ApiResponse<List<CrawledJobPost>> crawl(@RequestParam String keyword) {
        return ApiResponse.ok(jobKoreaCrawlerService.crawlDeveloperJobs(keyword, 10));
    }

    // 기존 코드 유지...
    @GetMapping
    public ApiResponse<List<JobPostResponse>> jobs() {
        return ApiResponse.ok(jobService.getAll());
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
