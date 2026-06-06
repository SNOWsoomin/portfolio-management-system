package com.example.portfolio.controller;

import com.example.portfolio.dto.ApiResponse;
import com.example.portfolio.dto.Requests.ProjectRequest;
import com.example.portfolio.dto.Responses.ProjectResponse;
import com.example.portfolio.service.CurrentUserService;
import com.example.portfolio.service.ProjectService;
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
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final CurrentUserService currentUserService;

    public ProjectController(ProjectService projectService, CurrentUserService currentUserService) {
        this.projectService = projectService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ApiResponse<ProjectResponse> create(Authentication authentication, @Valid @RequestBody ProjectRequest request) {
        return ApiResponse.ok(projectService.create(currentUserService.getCurrentUser(authentication), request));
    }

    @GetMapping("/me")
    public ApiResponse<List<ProjectResponse>> mine(Authentication authentication) {
        return ApiResponse.ok(projectService.getMine(currentUserService.getCurrentUser(authentication)));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProjectResponse> one(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok(projectService.getOne(currentUserService.getCurrentUser(authentication), id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProjectResponse> update(Authentication authentication, @PathVariable Long id,
                                               @Valid @RequestBody ProjectRequest request) {
        return ApiResponse.ok(projectService.update(currentUserService.getCurrentUser(authentication), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(Authentication authentication, @PathVariable Long id) {
        projectService.delete(currentUserService.getCurrentUser(authentication), id);
        return ApiResponse.ok(null, "프로젝트가 삭제되었습니다.");
    }
}
