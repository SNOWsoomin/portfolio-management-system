package com.example.portfolio.controller;

import com.example.portfolio.dto.ApiResponse;
import com.example.portfolio.dto.Requests.PortfolioRequest;
import com.example.portfolio.dto.Responses.PortfolioResponse;
import com.example.portfolio.service.CurrentUserService;
import com.example.portfolio.service.PortfolioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {
    private final PortfolioService portfolioService;
    private final CurrentUserService currentUserService;

    public PortfolioController(PortfolioService portfolioService, CurrentUserService currentUserService) {
        this.portfolioService = portfolioService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ApiResponse<PortfolioResponse> create(Authentication authentication, @Valid @RequestBody PortfolioRequest request) {
        return ApiResponse.ok(portfolioService.create(currentUserService.getCurrentUser(authentication), request));
    }

    @GetMapping("/me")
    public ApiResponse<List<PortfolioResponse>> mine(Authentication authentication) {
        return ApiResponse.ok(portfolioService.getMine(currentUserService.getCurrentUser(authentication)));
    }

    @GetMapping("/{id}")
    public ApiResponse<PortfolioResponse> one(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok(portfolioService.getOne(currentUserService.getCurrentUser(authentication), id));
    }

    @PutMapping("/{id}")
    public ApiResponse<PortfolioResponse> update(Authentication authentication, @PathVariable Long id,
                                                 @Valid @RequestBody PortfolioRequest request) {
        return ApiResponse.ok(portfolioService.update(currentUserService.getCurrentUser(authentication), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(Authentication authentication, @PathVariable Long id) {
        portfolioService.delete(currentUserService.getCurrentUser(authentication), id);
        return ApiResponse.ok(null, "포트폴리오가 삭제되었습니다.");
    }
}
