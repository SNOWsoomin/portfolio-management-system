package com.example.portfolio.service;

import com.example.portfolio.dto.Requests.JobPostRequest;
import com.example.portfolio.dto.Requests.JobSkillRequest;
import com.example.portfolio.dto.CrawledJobPost;
import com.example.portfolio.dto.CrawlerResponses.JobCrawlerResponse;
import com.example.portfolio.dto.CrawlerResponses.JobCrawlerStatusResponse;
import com.example.portfolio.dto.Responses.JobPostResponse;
import com.example.portfolio.dto.Responses.JobSkillResponse;
import com.example.portfolio.dto.Responses.MatchResponse;
import com.example.portfolio.entity.JobPost;
import com.example.portfolio.entity.JobSkill;
import com.example.portfolio.entity.Skill;
import com.example.portfolio.entity.SkillLevel;
import com.example.portfolio.entity.User;
import com.example.portfolio.exception.AppException;
import com.example.portfolio.repository.JobPostRepository;
import com.example.portfolio.repository.JobSkillRepository;
import com.example.portfolio.repository.SkillRepository;
import com.example.portfolio.repository.UserSkillRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobService {
    private final JobPostRepository jobPostRepository;
    private final JobSkillRepository jobSkillRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final JobKoreaCrawlerService jobKoreaCrawlerService;
    private volatile JobCrawlerResponse lastCrawlerResponse;
    private volatile boolean crawlerRunning;
    private volatile String lastCrawlerError;

    public JobService(JobPostRepository jobPostRepository, JobSkillRepository jobSkillRepository,
                      SkillRepository skillRepository, UserSkillRepository userSkillRepository,
                      JobKoreaCrawlerService jobKoreaCrawlerService) {
        this.jobPostRepository = jobPostRepository;
        this.jobSkillRepository = jobSkillRepository;
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
        this.jobKoreaCrawlerService = jobKoreaCrawlerService;
    }

    @Transactional(readOnly = true)
    public List<JobPostResponse> getAll() {
        return jobPostRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public JobPostResponse getOne(Long id) {
        return toResponse(findJob(id));
    }

    @Transactional
    public JobPostResponse create(JobPostRequest request) {
        JobPost jobPost = new JobPost();
        apply(jobPost, request);
        JobPost saved = jobPostRepository.save(jobPost);
        replaceSkills(saved, request.skills());
        return toResponse(saved);
    }

    @Transactional
    public JobPostResponse update(Long id, JobPostRequest request) {
        JobPost jobPost = findJob(id);
        apply(jobPost, request);
        replaceSkills(jobPost, request.skills());
        return toResponse(jobPost);
    }

    @Transactional
    public void delete(Long id) {
        JobPost jobPost = findJob(id);
        jobSkillRepository.deleteByJobPost(jobPost);
        jobPostRepository.delete(jobPost);
    }

    @Transactional
    public synchronized JobCrawlerResponse crawlJobKoreaDeveloperJobs(String keyword, int limit) {
        crawlerRunning = true;
        lastCrawlerError = null;
        try {
            String normalizedKeyword = keyword == null || keyword.isBlank() ? "개발자" : keyword.trim();
            List<CrawledJobPost> crawledJobs = jobKoreaCrawlerService.crawlDeveloperJobs(keyword, limit);
            LocalDateTime crawledAt = LocalDateTime.now();
            List<JobPostResponse> savedJobs = new ArrayList<>();

            for (CrawledJobPost crawledJob : crawledJobs) {
                JobPost jobPost = jobPostRepository
                        .findBySourceNameAndExternalId(jobKoreaCrawlerService.sourceName(), crawledJob.externalId())
                        .orElseGet(JobPost::new);
                jobPost.setTitle(crawledJob.title());
                jobPost.setCompanyName(crawledJob.companyName());
                jobPost.setPosition(crawledJob.position());
                jobPost.setDescription(crawledJob.description());
                jobPost.setSourceName(jobKoreaCrawlerService.sourceName());
                jobPost.setSourceUrl(crawledJob.sourceUrl());
                jobPost.setExternalId(crawledJob.externalId());
                jobPost.setCrawledAt(crawledAt);

                JobPost saved = jobPostRepository.save(jobPost);
                replaceSkillsByName(saved, crawledJob.skillNames());
                savedJobs.add(toResponse(saved));
            }

            JobCrawlerResponse response = new JobCrawlerResponse(
                    jobKoreaCrawlerService.sourceName(),
                    normalizedKeyword,
                    Math.max(1, Math.min(limit, 60)),
                    crawledJobs.size(),
                    savedJobs.size(),
                    crawledAt,
                    savedJobs
            );
            lastCrawlerResponse = response;
            return response;
        } catch (RuntimeException exception) {
            lastCrawlerError = exception.getMessage();
            throw exception;
        } finally {
            crawlerRunning = false;
        }
    }

    @Transactional
    public void runAutomaticJobKoreaCrawl() {
        try {
            crawlJobKoreaDeveloperJobs("개발자", 30);
        } catch (RuntimeException exception) {
            lastCrawlerError = exception.getMessage();
            crawlerRunning = false;
        }
    }

    @Transactional(readOnly = true)
    public JobCrawlerStatusResponse getCrawlerStatus() {
        JobCrawlerResponse last = lastCrawlerResponse;
        return new JobCrawlerStatusResponse(
                jobKoreaCrawlerService.sourceName(),
                last == null ? "개발자" : last.keyword(),
                last == null ? 30 : last.requestedLimit(),
                last == null ? 0 : last.fetchedCount(),
                last == null ? 0 : last.savedCount(),
                (int) jobPostRepository.count(),
                last == null ? null : last.crawledAt(),
                crawlerRunning,
                lastCrawlerError
        );
    }

    @Transactional(readOnly = true)
    public MatchResponse match(User user, Long jobId) {
        JobPost jobPost = findJob(jobId);
        return calculate(user, jobPost);
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> matchAll(User user) {
        return jobPostRepository.findAll().stream()
                .map(jobPost -> calculate(user, jobPost))
                .sorted(Comparator.comparingDouble(MatchResponse::matchRate).reversed())
                .toList();
    }

    private MatchResponse calculate(User user, JobPost jobPost) {
        Set<String> userSkillNames = userSkillRepository.findByUser(user).stream()
                .map(userSkill -> userSkill.getSkill().getName())
                .collect(Collectors.toSet());
        List<String> required = jobSkillRepository.findByJobPost(jobPost).stream()
                .map(jobSkill -> jobSkill.getSkill().getName())
                .toList();
        List<String> matched = required.stream().filter(userSkillNames::contains).toList();
        List<String> missing = required.stream().filter(skill -> !userSkillNames.contains(skill)).toList();
        double rate = required.isEmpty() ? 0 : Math.round((matched.size() * 10000.0) / required.size()) / 100.0;
        return new MatchResponse(jobPost.getId(), jobPost.getTitle(), jobPost.getCompanyName(), rate, matched, missing);
    }

    private JobPostResponse toResponse(JobPost jobPost) {
        List<JobSkillResponse> skills = jobSkillRepository.findByJobPost(jobPost).stream()
                .map(JobSkillResponse::from)
                .toList();
        return JobPostResponse.from(jobPost, skills);
    }

    private void apply(JobPost jobPost, JobPostRequest request) {
        jobPost.setTitle(request.title());
        jobPost.setCompanyName(request.companyName());
        jobPost.setDescription(request.description());
        jobPost.setPosition(request.position());
        jobPost.setSourceName(request.sourceName());
        jobPost.setSourceUrl(request.sourceUrl());
        jobPost.setExternalId(request.externalId());
    }

    private void replaceSkills(JobPost jobPost, List<JobSkillRequest> skills) {
        jobSkillRepository.deleteByJobPost(jobPost);
        if (skills == null) {
            return;
        }
        for (JobSkillRequest skillRequest : skills) {
            Skill skill = skillRepository.findById(skillRequest.skillId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "기술 스택을 찾을 수 없습니다."));
            JobSkill jobSkill = new JobSkill();
            jobSkill.setJobPost(jobPost);
            jobSkill.setSkill(skill);
            jobSkill.setRequiredLevel(skillRequest.requiredLevel() == null ? SkillLevel.BEGINNER : skillRequest.requiredLevel());
            jobSkillRepository.save(jobSkill);
        }
    }

    private void replaceSkillsByName(JobPost jobPost, List<String> skillNames) {
        jobSkillRepository.deleteByJobPost(jobPost);
        if (skillNames == null || skillNames.isEmpty()) {
            return;
        }

        for (String skillName : skillNames) {
            Skill skill = skillRepository.findByName(skillName)
                    .orElseGet(() -> createSkill(skillName));
            JobSkill jobSkill = new JobSkill();
            jobSkill.setJobPost(jobPost);
            jobSkill.setSkill(skill);
            jobSkill.setRequiredLevel(SkillLevel.BEGINNER);
            jobSkillRepository.save(jobSkill);
        }
    }

    private Skill createSkill(String skillName) {
        Skill skill = new Skill();
        skill.setName(skillName);
        skill.setCategory(JobKoreaCrawlerService.categoryOf(skillName));
        return skillRepository.save(skill);
    }

    private JobPost findJob(Long id) {
        return jobPostRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "채용공고를 찾을 수 없습니다."));
    }
}
