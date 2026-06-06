package com.example.portfolio.controller;

import com.example.portfolio.dto.AiReviewDtos.AiDiagnosisResponse;
import com.example.portfolio.dto.AiReviewDtos.AiFullReviewResponse;
import com.example.portfolio.dto.AiReviewDtos.AiPolishResponse;
import com.example.portfolio.dto.AiReviewDtos.AiReviewRequest;
import com.example.portfolio.dto.ApiResponse;
import com.example.portfolio.service.AiReviewService;
import com.example.portfolio.service.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-review")
public class AiReviewController {
    private final AiReviewService aiReviewService;
    private final CurrentUserService currentUserService;

    public AiReviewController(AiReviewService aiReviewService, CurrentUserService currentUserService) {
        this.aiReviewService = aiReviewService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/diagnose")
    public ApiResponse<AiDiagnosisResponse> diagnose(Authentication authentication, @RequestBody AiReviewRequest request) {
        return ApiResponse.ok(aiReviewService.diagnose(currentUserService.getCurrentUser(authentication), request));
    }

    @PostMapping("/full")
    public ApiResponse<AiFullReviewResponse> fullReview(Authentication authentication, @RequestBody AiReviewRequest request) {
        return ApiResponse.ok(aiReviewService.fullReview(currentUserService.getCurrentUser(authentication), request));
    }

    @PostMapping("/polish")
    public ApiResponse<AiPolishResponse> polish(Authentication authentication, @RequestBody AiReviewRequest request) {
        return ApiResponse.ok(aiReviewService.polish(currentUserService.getCurrentUser(authentication), request));
    }
}
