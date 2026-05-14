package com.example.portfolio.repository;

import com.example.portfolio.entity.Project;
import com.example.portfolio.entity.ProjectSkill;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectSkillRepository extends JpaRepository<ProjectSkill, Long> {
    List<ProjectSkill> findByProject(Project project);
    void deleteByProject(Project project);
}
