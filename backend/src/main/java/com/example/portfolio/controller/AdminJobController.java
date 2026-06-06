package com.example.portfolio.controller;

import com.example.portfolio.dto.ApiResponse;
import com.example.portfolio.dto.CrawlerResponses.JobCrawlerResponse;
import com.example.portfolio.dto.Requests.JobPostRequest;
import com.example.portfolio.dto.Responses.JobPostResponse;
import com.example.portfolio.service.JobService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/jobs")
public class AdminJobController {
    private final JobService jobService;

    public AdminJobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ApiResponse<JobPostResponse> create(@Valid @RequestBody JobPostRequest request) {
        return ApiResponse.ok(jobService.create(request), "채용공고가 등록되었습니다.");
    }

    @PostMapping("/crawl/jobkorea")
    public ApiResponse<JobCrawlerResponse> crawlJobKorea(
            @RequestParam(defaultValue = "개발자") String keyword,
            @RequestParam(defaultValue = "30") int limit
    ) {
        return ApiResponse.ok(jobService.crawlJobKoreaDeveloperJobs(keyword, limit), "잡코리아 개발자 공고를 수집했습니다.");
    }

    @PutMapping("/{id}")
    public ApiResponse<JobPostResponse> update(@PathVariable Long id, @Valid @RequestBody JobPostRequest request) {
        return ApiResponse.ok(jobService.update(id, request), "채용공고가 수정되었습니다.");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        jobService.delete(id);
        return ApiResponse.ok(null, "채용공고가 삭제되었습니다.");
    }
}
