package com.example.portfolio.repository;

import com.example.portfolio.entity.JobPost;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostRepository extends JpaRepository<JobPost, Long> {
    Optional<JobPost> findBySourceNameAndExternalId(String sourceName, String externalId);
}
