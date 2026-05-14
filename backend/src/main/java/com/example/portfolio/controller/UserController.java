package com.example.portfolio.controller;

import com.example.portfolio.dto.ApiResponse;
import com.example.portfolio.dto.Requests.UserSkillRequest;
import com.example.portfolio.dto.Responses.UserResponse;
import com.example.portfolio.dto.Responses.UserSkillResponse;
import com.example.portfolio.repository.UserRepository;
import com.example.portfolio.service.CurrentUserService;
import com.example.portfolio.service.SkillService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {
    private final CurrentUserService currentUserService;
    private final SkillService skillService;
    private final UserRepository userRepository;

    public UserController(CurrentUserService currentUserService, SkillService skillService, UserRepository userRepository) {
        this.currentUserService = currentUserService;
        this.skillService = skillService;
        this.userRepository = userRepository;
    }

    @GetMapping("/users/me")
    public ApiResponse<UserResponse> me(Authentication authentication) {
        return ApiResponse.ok(UserResponse.from(currentUserService.getCurrentUser(authentication)));
    }

    @GetMapping("/users/me/skills")
    public ApiResponse<List<UserSkillResponse>> mySkills(Authentication authentication) {
        return ApiResponse.ok(skillService.getUserSkills(currentUserService.getCurrentUser(authentication)));
    }

    @PostMapping("/users/me/skills")
    public ApiResponse<UserSkillResponse> addSkill(Authentication authentication, @Valid @RequestBody UserSkillRequest request) {
        return ApiResponse.ok(skillService.addUserSkill(currentUserService.getCurrentUser(authentication), request));
    }

    @DeleteMapping("/users/me/skills/{skillId}")
    public ApiResponse<Void> deleteSkill(Authentication authentication, @PathVariable Long skillId) {
        skillService.deleteUserSkill(currentUserService.getCurrentUser(authentication), skillId);
        return ApiResponse.ok(null, "기술 스택이 삭제되었습니다.");
    }

    @GetMapping("/admin/users")
    public ApiResponse<List<UserResponse>> users() {
        return ApiResponse.ok(userRepository.findAll().stream().map(UserResponse::from).toList());
    }
}
