package com.example.portfolio.config;

import com.example.portfolio.entity.*;
import com.example.portfolio.repository.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataSeeder {
    @Bean
    @Transactional
    CommandLineRunner seedData(
            UserRepository userRepository,
            SkillRepository skillRepository,
            UserSkillRepository userSkillRepository,
            PortfolioRepository portfolioRepository,
            ProjectRepository projectRepository,
            ProjectSkillRepository projectSkillRepository,
            JobPostRepository jobPostRepository,
            JobSkillRepository jobSkillRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            User admin = createUser("admin@test.com", "admin1234", "관리자", Role.ADMIN, passwordEncoder);
            User user = createUser("user@test.com", "user1234", "홍길동", Role.USER, passwordEncoder);
            userRepository.saveAll(List.of(admin, user));

            List<Skill> skills = List.of(
                    skill("Java", "Backend"),
                    skill("Spring Boot", "Backend"),
                    skill("JPA", "Backend"),
                    skill("Spring Security", "Backend"),
                    skill("React", "Frontend"),
                    skill("JavaScript", "Frontend"),
                    skill("HTML", "Frontend"),
                    skill("CSS", "Frontend"),
                    skill("MySQL", "Database"),
                    skill("H2", "Database"),
                    skill("PostgreSQL", "Database"),
                    skill("Redis", "Database"),
                    skill("GitHub", "Tool"),
                    skill("Figma", "Tool"),
                    skill("REST API", "Backend"),
                    skill("TypeScript", "Frontend"),
                    skill("Next.js", "Frontend"),
                    skill("Docker", "DevOps"),
                    skill("Linux", "DevOps"),
                    skill("CI/CD", "DevOps"),
                    skill("AWS", "DevOps")
            );
            skillRepository.saveAll(skills);
            Map<String, Skill> skillMap = skillRepository.findAll().stream()
                    .collect(Collectors.toMap(Skill::getName, s -> s));

            addUserSkill(userSkillRepository, user, skillMap.get("Java"), SkillLevel.INTERMEDIATE);
            addUserSkill(userSkillRepository, user, skillMap.get("Spring Boot"), SkillLevel.INTERMEDIATE);
            addUserSkill(userSkillRepository, user, skillMap.get("React"), SkillLevel.BEGINNER);
            addUserSkill(userSkillRepository, user, skillMap.get("JavaScript"), SkillLevel.INTERMEDIATE);
            addUserSkill(userSkillRepository, user, skillMap.get("HTML"), SkillLevel.INTERMEDIATE);
            addUserSkill(userSkillRepository, user, skillMap.get("CSS"), SkillLevel.INTERMEDIATE);
            addUserSkill(userSkillRepository, user, skillMap.get("GitHub"), SkillLevel.INTERMEDIATE);
            addUserSkill(userSkillRepository, user, skillMap.get("Docker"), SkillLevel.BEGINNER);

            Portfolio portfolio = new Portfolio();
            portfolio.setUser(user);
            portfolio.setTitle("홍길동 기술 포트폴리오");
            portfolio.setIntroduction("Java/Spring Boot와 React 기반 웹 서비스를 만들고 성장 중인 주니어 개발자입니다.");
            portfolio.setMarkdownContent("""
                    ## 핵심 역량
                    - Java와 Spring Boot 기반 REST API 설계 및 구현
                    - React 기반 포트폴리오/관리자 화면 구현
                    - GitHub 협업 플로우와 Docker 기초 배포 경험
                    - 채용공고 요구 기술을 기준으로 부족 역량을 분석하고 보완

                    ## 대표 경험
                    사용자 인증, 프로젝트 CRUD, 기술 스택 매칭률 계산, 차트 시각화, PDF 내보내기까지 이어지는 웹 서비스를 직접 구현했습니다.

                    ## 취업 준비 방향
                    JPA 심화, Spring Security, MySQL 인덱싱, AWS 배포 역량을 집중 보완할 계획입니다.
                    """);
            portfolio.setPublic(true);
            portfolioRepository.save(portfolio);

            Project library = createProject(user, portfolio, "도서 대여 관리 시스템",
                    "도서 등록, 대여, 반납을 관리하는 웹 서비스", "백엔드 API와 React 화면 구현",
                    LocalDate.of(2025, 3, 1), LocalDate.of(2025, 6, 30), "https://github.com/sample/library", "");
            Project portfolioProject = createProject(user, portfolio, "포트폴리오 관리 시스템",
                    "기술 스택과 포트폴리오를 관리하고 채용공고와 비교하는 서비스", "풀스택 개발",
                    LocalDate.of(2025, 9, 1), LocalDate.of(2025, 12, 15), "https://github.com/sample/portfolio", "");
            Project teamDashboard = createProject(user, portfolio, "팀 프로젝트 협업 대시보드",
                    "팀별 일정, 역할, 산출물을 카드와 차트로 관리하는 React 대시보드", "프론트엔드 화면 설계와 컴포넌트 구현",
                    LocalDate.of(2025, 7, 1), LocalDate.of(2025, 8, 20), "https://github.com/sample/team-dashboard", "https://team-dashboard.example.com");
            Project deployLab = createProject(user, portfolio, "Docker 배포 실습 랩",
                    "Spring Boot 애플리케이션을 Docker로 패키징하고 Linux 환경에서 실행한 배포 실습", "Dockerfile 작성, 실행 스크립트 정리",
                    LocalDate.of(2026, 1, 5), LocalDate.of(2026, 2, 10), "https://github.com/sample/docker-lab", "");
            projectRepository.saveAll(List.of(library, portfolioProject, teamDashboard, deployLab));
            addProjectSkills(projectSkillRepository, library, List.of(skillMap.get("Java"), skillMap.get("Spring Boot"), skillMap.get("MySQL"), skillMap.get("React")));
            addProjectSkills(projectSkillRepository, portfolioProject, List.of(skillMap.get("Spring Boot"), skillMap.get("JPA"), skillMap.get("React"), skillMap.get("GitHub")));
            addProjectSkills(projectSkillRepository, teamDashboard, List.of(skillMap.get("React"), skillMap.get("JavaScript"), skillMap.get("HTML"), skillMap.get("CSS"), skillMap.get("Figma")));
            addProjectSkills(projectSkillRepository, deployLab, List.of(skillMap.get("Docker"), skillMap.get("Linux"), skillMap.get("GitHub"), skillMap.get("Spring Boot")));

            createJob(jobPostRepository, jobSkillRepository, skillMap, "백엔드 개발자", "테크랩", "Java/Spring 기반 API와 데이터 모델을 설계하는 주니어 백엔드 포지션", "Backend Developer",
                    List.of("Java", "Spring Boot", "JPA", "MySQL"));
            createJob(jobPostRepository, jobSkillRepository, skillMap, "프론트엔드 개발자", "웹프렌즈", "React 기반 서비스 화면과 반응형 UI를 구현하는 포지션", "Frontend Developer",
                    List.of("React", "JavaScript", "HTML", "CSS"));
            createJob(jobPostRepository, jobSkillRepository, skillMap, "풀스택 개발자", "스타트업허브", "React와 Spring Boot 기반 MVP를 빠르게 구현하는 포지션", "Fullstack Developer",
                    List.of("React", "Java", "Spring Boot", "MySQL"));
            createJob(jobPostRepository, jobSkillRepository, skillMap, "주니어 웹 개발자", "커리어브릿지", "웹 서비스 기본기를 갖춘 신입/주니어 개발자 채용", "Junior Web Developer",
                    List.of("Java", "Spring Boot", "React", "GitHub"));
            createJob(jobPostRepository, jobSkillRepository, skillMap, "React TypeScript 개발자", "인터랙션랩", "React와 TypeScript로 운영자 화면과 디자인 시스템을 구현하는 포지션", "Frontend Engineer",
                    List.of("React", "TypeScript", "Next.js", "CSS"));
            createJob(jobPostRepository, jobSkillRepository, skillMap, "Spring Security 백엔드", "세이프API", "인증/인가와 보안이 중요한 서비스의 API 서버를 개발하는 포지션", "Backend Security Engineer",
                    List.of("Java", "Spring Boot", "Spring Security", "JPA", "Redis"));
            createJob(jobPostRepository, jobSkillRepository, skillMap, "DevOps형 웹 개발자", "클라우드웨이브", "Docker와 Linux 기반 배포 파이프라인을 이해하는 웹 개발자 포지션", "Web/DevOps Developer",
                    List.of("Docker", "Linux", "AWS", "GitHub", "CI/CD"));
            createJob(jobPostRepository, jobSkillRepository, skillMap, "클라우드 풀스택 인턴", "넥스트커리어", "Spring Boot, React, Docker를 활용해 클라우드 환경의 서비스를 경험하는 인턴십", "Fullstack Intern",
                    List.of("Java", "Spring Boot", "React", "Docker", "AWS", "MySQL"));
        };
    }

    private User createUser(String email, String rawPassword, String name, Role role, PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setName(name);
        user.setRole(role);
        return user;
    }

    private Skill skill(String name, String category) {
        Skill skill = new Skill();
        skill.setName(name);
        skill.setCategory(category);
        return skill;
    }

    private void addUserSkill(UserSkillRepository repository, User user, Skill skill, SkillLevel level) {
        UserSkill userSkill = new UserSkill();
        userSkill.setUser(user);
        userSkill.setSkill(skill);
        userSkill.setLevel(level);
        repository.save(userSkill);
    }

    private Project createProject(User user, Portfolio portfolio, String title, String description, String roleDescription,
                                  LocalDate startDate, LocalDate endDate, String githubUrl, String deployUrl) {
        Project project = new Project();
        project.setUser(user);
        project.setPortfolio(portfolio);
        project.setTitle(title);
        project.setDescription(description);
        project.setRoleDescription(roleDescription);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        project.setGithubUrl(githubUrl);
        project.setDeployUrl(deployUrl);
        return project;
    }

    private void addProjectSkills(ProjectSkillRepository repository, Project project, List<Skill> skills) {
        for (Skill skill : skills) {
            ProjectSkill projectSkill = new ProjectSkill();
            projectSkill.setProject(project);
            projectSkill.setSkill(skill);
            repository.save(projectSkill);
        }
    }

    private void createJob(JobPostRepository jobPostRepository, JobSkillRepository jobSkillRepository, Map<String, Skill> skillMap,
                           String title, String company, String description, String position, List<String> skillNames) {
        JobPost job = new JobPost();
        job.setTitle(title);
        job.setCompanyName(company);
        job.setDescription(description);
        job.setPosition(position);
        jobPostRepository.save(job);
        for (String skillName : skillNames) {
            JobSkill jobSkill = new JobSkill();
            jobSkill.setJobPost(job);
            jobSkill.setSkill(skillMap.get(skillName));
            jobSkill.setRequiredLevel(SkillLevel.BEGINNER);
            jobSkillRepository.save(jobSkill);
        }
    }
}
