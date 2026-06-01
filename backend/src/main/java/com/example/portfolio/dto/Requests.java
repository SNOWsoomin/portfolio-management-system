package com.example.portfolio.dto;

import com.example.portfolio.entity.SkillLevel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class Requests {
    public record SignupRequest(@Email @NotBlank String email, @NotBlank String password, @NotBlank String name) {}
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record PortfolioRequest(@NotBlank String title, String introduction, String markdownContent, boolean isPublic) {}
    public record ProjectRequest(
            @NotBlank String title,
            String description,
            String roleDescription,
            LocalDate startDate,
            LocalDate endDate,
            String githubUrl,
            String deployUrl,
            Long portfolioId,
            List<Long> skillIds
    ) {}
    public record UserSkillRequest(@NotNull Long skillId, @NotNull SkillLevel level) {}
    public record JobSkillRequest(@NotNull Long skillId, SkillLevel requiredLevel) {}
    public record JobPostRequest(
            @NotBlank String title,
            @NotBlank String companyName,
            String description,
            @NotBlank String position,
            String sourceName,
            String sourceUrl,
            String externalId,
            List<JobSkillRequest> skills
    ) {}
}
