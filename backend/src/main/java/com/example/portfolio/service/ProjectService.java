package com.example.portfolio.service;

import com.example.portfolio.dto.Requests.ProjectRequest;
import com.example.portfolio.dto.Responses.ProjectResponse;
import com.example.portfolio.dto.Responses.SkillResponse;
import com.example.portfolio.entity.Portfolio;
import com.example.portfolio.entity.Project;
import com.example.portfolio.entity.ProjectSkill;
import com.example.portfolio.entity.Skill;
import com.example.portfolio.entity.User;
import com.example.portfolio.exception.AppException;
import com.example.portfolio.repository.PortfolioRepository;
import com.example.portfolio.repository.ProjectRepository;
import com.example.portfolio.repository.ProjectSkillRepository;
import com.example.portfolio.repository.SkillRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectSkillRepository projectSkillRepository;
    private final SkillRepository skillRepository;
    private final PortfolioRepository portfolioRepository;

    public ProjectService(ProjectRepository projectRepository, ProjectSkillRepository projectSkillRepository,
                          SkillRepository skillRepository, PortfolioRepository portfolioRepository) {
        this.projectRepository = projectRepository;
        this.projectSkillRepository = projectSkillRepository;
        this.skillRepository = skillRepository;
        this.portfolioRepository = portfolioRepository;
    }

    @Transactional
    public ProjectResponse create(User user, ProjectRequest request) {
        Project project = new Project();
        project.setUser(user);
        apply(project, user, request);
        Project saved = projectRepository.save(project);
        replaceSkills(saved, request.skillIds());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getMine(User user) {
        return projectRepository.findByUserOrderByCreatedAtDesc(user).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getOne(User user, Long id) {
        return toResponse(findOwned(user, id));
    }

    @Transactional
    public ProjectResponse update(User user, Long id, ProjectRequest request) {
        Project project = findOwned(user, id);
        apply(project, user, request);
        replaceSkills(project, request.skillIds());
        return toResponse(project);
    }

    @Transactional
    public void delete(User user, Long id) {
        Project project = findOwned(user, id);
        projectSkillRepository.deleteByProject(project);
        projectRepository.delete(project);
    }

    public ProjectResponse toResponse(Project project) {
        List<SkillResponse> skills = projectSkillRepository.findByProject(project).stream()
                .map(ProjectSkill::getSkill)
                .map(SkillResponse::from)
                .toList();
        return ProjectResponse.from(project, skills);
    }

    private void apply(Project project, User user, ProjectRequest request) {
        project.setTitle(request.title());
        project.setDescription(request.description());
        project.setRoleDescription(request.roleDescription());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        project.setGithubUrl(request.githubUrl());
        project.setDeployUrl(request.deployUrl());
        if (request.portfolioId() == null) {
            project.setPortfolio(null);
            return;
        }
        Portfolio portfolio = portfolioRepository.findById(request.portfolioId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "포트폴리오를 찾을 수 없습니다."));
        if (!portfolio.getUser().getId().equals(user.getId())) {
            throw new AppException(HttpStatus.FORBIDDEN, "본인 포트폴리오에만 프로젝트를 연결할 수 있습니다.");
        }
        project.setPortfolio(portfolio);
    }

    private void replaceSkills(Project project, List<Long> skillIds) {
        projectSkillRepository.deleteByProject(project);
        if (skillIds == null) {
            return;
        }
        for (Long skillId : skillIds.stream().distinct().toList()) {
            Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "기술 스택을 찾을 수 없습니다."));
            ProjectSkill projectSkill = new ProjectSkill();
            projectSkill.setProject(project);
            projectSkill.setSkill(skill);
            projectSkillRepository.save(projectSkill);
        }
    }

    private Project findOwned(User user, Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
        if (!project.getUser().getId().equals(user.getId())) {
            throw new AppException(HttpStatus.FORBIDDEN, "본인 프로젝트만 접근할 수 있습니다.");
        }
        return project;
    }
}
