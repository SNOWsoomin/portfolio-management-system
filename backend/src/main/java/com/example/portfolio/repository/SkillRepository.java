package com.example.portfolio.repository;

import com.example.portfolio.entity.Skill;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByName(String name);
}
