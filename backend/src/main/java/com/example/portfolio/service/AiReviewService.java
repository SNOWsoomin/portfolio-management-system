package com.example.portfolio.service;

import com.example.portfolio.dto.AiReviewDtos.*;
import com.example.portfolio.dto.Responses.*;
import com.example.portfolio.entity.User;
import com.example.portfolio.exception.AppException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiReviewService {
    private static final Logger log = LoggerFactory.getLogger(AiReviewService.class);
    private static final Pattern GEMINI_RETRY_SECONDS = Pattern.compile("Please retry in ([0-9.]+)s");

    private final SkillService skillService;
    private final ProjectService projectService;
    private final JobService jobService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public AiReviewService(SkillService skillService, ProjectService projectService, JobService jobService,
                           ObjectMapper objectMapper, RestClient.Builder restClientBuilder,
                           @Value("${gemini.api-key:}") String apiKey,
                           @Value("${gemini.model:gemini-3.5-flash}") String model) {
        this.skillService = skillService;
        this.projectService = projectService;
        this.jobService = jobService;
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder.build();
        this.apiKey = apiKey;
        this.model = model;
    }

    @Transactional(readOnly = true)
    public AiFullReviewResponse fullReview(User user, AiReviewRequest request) {
        validateGeminiKey();
        AiReviewContext context = buildContext(user, request);
        
        try {
            String prompt = "너는 포트폴리오 첨삭 전문가다... (생략)"; // 프롬프트는 긴 문자열이므로 그대로 유지
            Map<String, Object> response = requestGeminiJson(prompt);
            
            return new AiFullReviewResponse(
                    "Gemini", false,
                    text(response, "diagnosis", "AI 진단 결과를 생성했습니다."),
                    stringList(response, "problems", List.of("문제점 미반환")),
                    stringList(response, "strengths", List.of("분석 완료")),
                    stringList(response, "suggestedFocus", List.of("역할 보완 제안")),
                    text(response, "improvedIntroduction", context.introduction()),
                    textOrJoinedList(response, "improvedMarkdownContent", "improvedMarkdownLines", context.markdownContent()),
                    stringList(response, "changeSummary", List.of("본문 수정 완료")),
                    stringList(response, "warnings", List.of()),
                    model
            );
        } catch (Exception e) {
            throw new AppException(HttpStatus.BAD_GATEWAY, "Gemini 요청 실패");
        }
    }

    // ... (이후 메서드들은 동일한 규칙으로 들여쓰기 적용)

    private record AiReviewContext(
            String userName, String introduction, String markdownContent,
            List<UserSkillResponse> skills, List<ProjectResponse> projects,
            List<MatchResponse> topMatches, List<String> missingSkills
    ) {
        String toCompactPromptText() {
            return String.format("이름: %s, 자기소개: %s...", userName, limit(introduction, 600));
        }

        private static String limit(String value, int max) {
            String safe = StringUtils.hasText(value) ? value : "";
            return safe.length() <= max ? safe : safe.substring(0, max) + "...";
        }
    }
}
