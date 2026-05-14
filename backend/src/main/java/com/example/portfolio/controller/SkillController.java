package com.example.portfolio.controller;

import com.example.portfolio.dto.ApiResponse;
import com.example.portfolio.dto.Responses.SkillResponse;
import com.example.portfolio.service.SkillService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skills")
public class SkillController {
    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    public ApiResponse<List<SkillResponse>> skills() {
        return ApiResponse.ok(skillService.getAllSkills());
    }
}
