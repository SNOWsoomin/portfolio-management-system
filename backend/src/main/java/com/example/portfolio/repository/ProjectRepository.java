package com.example.portfolio.repository;

import com.example.portfolio.entity.Project;
import com.example.portfolio.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUserOrderByCreatedAtDesc(User user);
}
