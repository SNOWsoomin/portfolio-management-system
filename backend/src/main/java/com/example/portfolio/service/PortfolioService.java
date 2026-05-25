package com.example.portfolio.service;

import com.example.portfolio.dto.Requests.PortfolioRequest;
import com.example.portfolio.dto.Responses.PortfolioResponse;
import com.example.portfolio.entity.Portfolio;
import com.example.portfolio.entity.Role;
import com.example.portfolio.entity.User;
import com.example.portfolio.exception.AppException;
import com.example.portfolio.repository.PortfolioRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;

    public PortfolioService(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    @Transactional
    public PortfolioResponse create(User user, PortfolioRequest request) {
        Portfolio portfolio = new Portfolio();
        portfolio.setUser(user);
        apply(portfolio, request);
        return PortfolioResponse.from(portfolioRepository.save(portfolio));
    }

    @Transactional(readOnly = true)
    public List<PortfolioResponse> getMine(User user) {
        return portfolioRepository.findByUserOrderByCreatedAtDesc(user).stream().map(PortfolioResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getOne(User user, Long id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "포트폴리오를 찾을 수 없습니다."));
        boolean owner = portfolio.getUser().getId().equals(user.getId());
        if (!owner && !portfolio.isPublic() && user.getRole() != Role.ADMIN) {
            throw new AppException(HttpStatus.FORBIDDEN, "비공개 포트폴리오입니다.");
        }
        return PortfolioResponse.from(portfolio);
    }

    @Transactional
    public PortfolioResponse update(User user, Long id, PortfolioRequest request) {
        Portfolio portfolio = findOwned(user, id);
        apply(portfolio, request);
        return PortfolioResponse.from(portfolio);
    }

    @Transactional
    public void delete(User user, Long id) {
        portfolioRepository.delete(findOwned(user, id));
    }

    private Portfolio findOwned(User user, Long id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "포트폴리오를 찾을 수 없습니다."));
        if (!portfolio.getUser().getId().equals(user.getId())) {
            throw new AppException(HttpStatus.FORBIDDEN, "본인 포트폴리오만 수정할 수 있습니다.");
        }
        return portfolio;
    }

    private void apply(Portfolio portfolio, PortfolioRequest request) {
        portfolio.setTitle(request.title());
        portfolio.setIntroduction(request.introduction());
        portfolio.setMarkdownContent(request.markdownContent());
        portfolio.setPublic(request.isPublic());
    }
}
