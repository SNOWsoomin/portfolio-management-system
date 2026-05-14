package com.example.portfolio.repository;

import com.example.portfolio.entity.JobPost;
import com.example.portfolio.entity.JobSkill;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {
    List<JobSkill> findByJobPost(JobPost jobPost);
    void deleteByJobPost(JobPost jobPost);
}
