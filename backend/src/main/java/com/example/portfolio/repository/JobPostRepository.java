package com.example.portfolio.repository;

import com.example.portfolio.entity.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JobPostRepository extends JpaRepository<JobPost, Long> {
    Optional<JobPost> findBySourceNameAndExternalId(String sourceName, String externalId);
}
