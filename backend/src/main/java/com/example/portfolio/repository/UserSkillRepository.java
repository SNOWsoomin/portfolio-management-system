package com.example.portfolio.repository;

import com.example.portfolio.entity.Skill;
import com.example.portfolio.entity.User;
import com.example.portfolio.entity.UserSkill;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    List<UserSkill> findByUser(User user);
    Optional<UserSkill> findByUserAndSkill(User user, Skill skill);
    void deleteByUserAndSkill(User user, Skill skill);
}
