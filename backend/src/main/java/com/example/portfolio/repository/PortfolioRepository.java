package com.example.portfolio.repository;

import com.example.portfolio.entity.Portfolio;
import com.example.portfolio.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByUserOrderByCreatedAtDesc(User user);
    Optional<Portfolio> findFirstByUserOrderByUpdatedAtDesc(User user);
}
