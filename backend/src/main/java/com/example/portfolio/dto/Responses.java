package com.example.portfolio.dto;

import com.example.portfolio.entity.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Responses {
    public record LoginResponse(String accessToken, String userName, String role) {}
    public record UserResponse(Long id, String email, String name, Role role, LocalDateTime createdAt) {
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getRole(), user.getCreatedAt());
        }
    }
    public record SkillResponse(Long id, String name, String category) {
        public static SkillResponse from(Skill skill) {
            return new SkillResponse(skill.getId(), skill.getName(), skill.getCategory());
        }
    }
    public record UserSkillResponse(Long skillId, String name, String category, SkillLevel level) {
        public static UserSkillResponse from(UserSkill userSkill) {
            Skill skill = userSkill.getSkill();
            return new UserSkillResponse(skill.getId(), skill.getName(), skill.getCategory(), userSkill.getLevel());
        }
    }
    public record PortfolioResponse(
            Long id,
            Long userId,
            String title,
            String introduction,
            String markdownContent,
            boolean isPublic,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static PortfolioResponse from(Portfolio portfolio) {
            return new PortfolioResponse(
                    portfolio.getId(),
                    portfolio.getUser().getId(),
                    portfolio.getTitle(),
                    portfolio.getIntroduction(),
                    portfolio.getMarkdownContent(),
                    portfolio.isPublic(),
                    portfolio.getCreatedAt(),
                    portfolio.getUpdatedAt()
            );
        }
    }
    public record ProjectResponse(
            Long id,
            Long portfolioId,
            String title,
            String description,
            String roleDescription,
            LocalDate startDate,
            LocalDate endDate,
            String githubUrl,
            String deployUrl,
            List<SkillResponse> skills
    ) {
        public static ProjectResponse from(Project project, List<SkillResponse> skills) {
            Long portfolioId = project.getPortfolio() == null ? null : project.getPortfolio().getId();
            return new ProjectResponse(
                    project.getId(),
                    portfolioId,
                    project.getTitle(),
                    project.getDescription(),
                    project.getRoleDescription(),
                    project.getStartDate(),
                    project.getEndDate(),
                    project.getGithubUrl(),
                    project.getDeployUrl(),
                    skills
            );
        }
    }
    public record JobSkillResponse(Long skillId, String name, String category, SkillLevel requiredLevel) {
        public static JobSkillResponse from(JobSkill jobSkill) {
            Skill skill = jobSkill.getSkill();
            return new JobSkillResponse(skill.getId(), skill.getName(), skill.getCategory(), jobSkill.getRequiredLevel());
        }
    }
    public record JobPostResponse(
            Long id,
            String title,
            String companyName,
            String description,
            String position,
            String sourceName,
            String sourceUrl,
            String externalId,
            LocalDateTime crawledAt,
            List<JobSkillResponse> skills
    ) {
        public static JobPostResponse from(JobPost jobPost, List<JobSkillResponse> skills) {
            return new JobPostResponse(
                    jobPost.getId(),
                    jobPost.getTitle(),
                    jobPost.getCompanyName(),
                    jobPost.getDescription(),
                    jobPost.getPosition(),
                    jobPost.getSourceName(),
                    jobPost.getSourceUrl(),
                    jobPost.getExternalId(),
                    jobPost.getCrawledAt(),
                    skills
            );
        }
    }
    public record MatchResponse(
            Long jobPostId,
            String jobTitle,
            String companyName,
            double matchRate,
            List<String> matchedSkills,
            List<String> missingSkills
    ) {}
}
