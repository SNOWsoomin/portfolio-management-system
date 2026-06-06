package com.example.portfolio.service;

import com.example.portfolio.dto.AiReviewDtos.AiDiagnosisResponse;
import com.example.portfolio.dto.AiReviewDtos.AiFullReviewResponse;
import com.example.portfolio.dto.AiReviewDtos.AiPolishResponse;
import com.example.portfolio.dto.AiReviewDtos.AiReviewRequest;
import com.example.portfolio.dto.Responses.MatchResponse;
import com.example.portfolio.dto.Responses.ProjectResponse;
import com.example.portfolio.dto.Responses.UserSkillResponse;
import com.example.portfolio.entity.User;
import com.example.portfolio.exception.AppException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

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
            String prompt = """
                    너는 Java/Spring/React 취업 포트폴리오 첨삭 전문가다.
                    아래 사용자의 포트폴리오 데이터를 분석하고, 진단 결과와 첨삭 수정본을 한 번에 생성해라.
                    반드시 JSON만 반환해라.

                    응답 스키마:
                    {
                      "diagnosis": "전체 진단 한 문단",
                      "problems": ["부족한 점 1", "부족한 점 2", "부족한 점 3"],
                      "strengths": ["강점 1", "강점 2"],
                      "suggestedFocus": ["추가/보완할 점 1", "추가/보완할 점 2", "추가/보완할 점 3"],
                      "improvedIntroduction": "첨삭된 자기소개",
                      "improvedMarkdownLines": ["## 핵심 역량", "- 내용 1", "## 대표 경험", "- 내용 2"],
                      "changeSummary": ["수정 요약 1", "수정 요약 2", "수정 요약 3"],
                      "warnings": ["주의사항 1"]
                    }

                    조건:
                    - 없는 경험을 지어내지 말고, 기존 프로젝트와 기술 스택만 사용한다.
                    - 부족 기술은 거짓 보유 역량으로 쓰지 말고 학습/보완 방향으로만 표현한다.
                    - 문제-해결-역할-성과 구조가 보이도록 다듬는다.
                    - 한국어로 작성하고, 발표/취업 포트폴리오에 어울리는 톤을 사용한다.
                    - 각 배열은 최대 3개 항목만 작성한다.
                    - improvedIntroduction은 250자 이내로 작성한다.
                    - improvedMarkdownLines 전체는 12줄 이내로 작성한다.
                    - 모든 문자열은 줄바꿈 없는 한 줄 문자열로 작성한다.
                    - 응답은 간결한 순수 JSON 객체만 작성하고, 코드블록은 사용하지 않는다.

                    [사용자 데이터]
                    %s
                    """.formatted(context.toCompactPromptText());
            Map<String, Object> response = requestGeminiJson(prompt);
            return new AiFullReviewResponse(
                    "Gemini",
                    false,
                    text(response, "diagnosis", "AI 진단 결과를 생성했습니다."),
                    stringList(response, "problems", List.of("구체적인 문제점이 반환되지 않았습니다.")),
                    stringList(response, "strengths", List.of("포트폴리오 데이터 기반 분석이 완료되었습니다.")),
                    stringList(response, "suggestedFocus", List.of("프로젝트 역할과 성과를 더 구체화하세요.")),
                    text(response, "improvedIntroduction", context.introduction()),
                    textOrJoinedList(response, "improvedMarkdownContent", "improvedMarkdownLines", context.markdownContent()),
                    stringList(response, "changeSummary", List.of("자기소개와 포트폴리오 본문을 다듬었습니다.")),
                    stringList(response, "warnings", List.of()),
                    model
            );
        } catch (Exception exception) {
            throw new AppException(HttpStatus.BAD_GATEWAY, "Gemini AI 첨삭 요청에 실패했습니다. API 키와 네트워크 상태를 확인하세요.");
        }
    }

    @Transactional(readOnly = true)
    public AiDiagnosisResponse diagnose(User user, AiReviewRequest request) {
        validateGeminiKey();
        AiReviewContext context = buildContext(user, request);
        try {
            String prompt = """
                    너는 Java/Spring/React 취업 포트폴리오 첨삭 전문가다.
                    아래 사용자의 포트폴리오 데이터를 분석해서 반드시 JSON만 반환해라.
                    응답 스키마:
                    {
                      "diagnosis": "전체 진단 한 문단",
                      "problems": ["문제점 1", "문제점 2", "문제점 3"],
                      "strengths": ["강점 1", "강점 2"],
                      "suggestedFocus": ["보완 방향 1", "보완 방향 2", "보완 방향 3"]
                    }
                    조건:
                    - 과장하지 말고 현재 데이터에 근거해서 판단한다.
                    - 부족 기술과 프로젝트 역할 설명을 반드시 반영한다.
                    - 한국어로 발표/취업 포트폴리오에 어울리게 작성한다.
                    - 각 배열은 최대 4개 항목만 작성한다.

                    [사용자 데이터]
                    %s
                    """.formatted(context.toCompactPromptText());
            Map<String, Object> response = requestGeminiJson(prompt);
            return new AiDiagnosisResponse(
                    "Gemini",
                    false,
                    text(response, "diagnosis", "AI 진단 결과를 생성했습니다."),
                    stringList(response, "problems", List.of("구체적인 문제점이 반환되지 않았습니다.")),
                    stringList(response, "strengths", List.of("포트폴리오 데이터 기반 분석이 완료되었습니다.")),
                    stringList(response, "suggestedFocus", List.of("프로젝트 역할과 성과를 더 구체화하세요.")),
                    model
            );
        } catch (Exception exception) {
            throw new AppException(HttpStatus.BAD_GATEWAY, "Gemini AI 첨삭 요청에 실패했습니다. API 키와 네트워크 상태를 확인하세요.");
        }
    }

    @Transactional(readOnly = true)
    public AiPolishResponse polish(User user, AiReviewRequest request) {
        validateGeminiKey();
        AiReviewContext context = buildContext(user, request);
        try {
            String prompt = """
                    너는 Java/Spring/React 취업 포트폴리오 첨삭 전문가다.
                    아래 진단 근거를 바탕으로 사용자의 자기소개와 Markdown 포트폴리오 본문을 더 설득력 있게 다듬어라.
                    반드시 JSON만 반환해라.
                    응답 스키마:
                    {
                      "diagnosis": "첨삭 방향 요약",
                      "improvedIntroduction": "수정된 자기소개",
                      "improvedMarkdownContent": "수정된 Markdown 본문",
                      "changeSummary": ["변경점 1", "변경점 2", "변경점 3"],
                      "warnings": ["주의사항 1"]
                    }
                    조건:
                    - 없는 경험을 새로 지어내지 말고, 기존 프로젝트와 기술 스택만 활용한다.
                    - 문제-해결-역할-성과 구조로 다듬는다.
                    - 채용공고 부족 기술은 학습/보완 방향으로만 제안한다.
                    - Markdown 형식은 유지하고 제목/목록을 읽기 좋게 정리한다.
                    - improvedMarkdownContent는 2500자 이내로 작성한다.
                    - 각 배열은 최대 4개 항목만 작성한다.

                    [사용자 데이터]
                    %s
                    """.formatted(context.toCompactPromptText());
            Map<String, Object> response = requestGeminiJson(prompt);
            return new AiPolishResponse(
                    "Gemini",
                    false,
                    text(response, "diagnosis", "AI 첨삭 예시본을 생성했습니다."),
                    text(response, "improvedIntroduction", context.introduction()),
                    text(response, "improvedMarkdownContent", context.markdownContent()),
                    stringList(response, "changeSummary", List.of("자기소개와 포트폴리오 본문을 다듬었습니다.")),
                    stringList(response, "warnings", List.of()),
                    model
            );
        } catch (Exception exception) {
            throw new AppException(HttpStatus.BAD_GATEWAY, "Gemini AI 첨삭 예시본 생성에 실패했습니다. API 키와 네트워크 상태를 확인하세요.");
        }
    }

    private AiReviewContext buildContext(User user, AiReviewRequest request) {
        List<UserSkillResponse> skills = skillService.getUserSkills(user);
        List<ProjectResponse> projects = projectService.getMine(user);
        List<MatchResponse> matches = jobService.matchAll(user);
        Set<String> missingSkills = new LinkedHashSet<>();
        matches.stream()
                .limit(5)
                .flatMap(match -> match.missingSkills().stream())
                .forEach(missingSkills::add);
        return new AiReviewContext(
                user.getName(),
                safe(request.introduction()),
                safe(request.markdownContent()),
                skills,
                projects,
                matches.stream().limit(5).toList(),
                missingSkills.stream().limit(6).toList()
        );
    }

    private Map<String, Object> requestGeminiJson(String prompt) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return requestGeminiJsonOnce(prompt);
            } catch (RestClientResponseException exception) {
                lastException = exception;
                String responseBody = exception.getResponseBodyAsString();
                log.warn("Gemini request failed. attempt={}, status={}, body={}",
                        attempt, exception.getStatusCode(), limitLog(responseBody));
                if (attempt == 3) {
                    break;
                }
                Thread.sleep(resolveRetryDelayMillis(responseBody, attempt));
            } catch (Exception exception) {
                lastException = exception;
                log.warn("Gemini request failed. attempt={}, message={}", attempt, exception.getMessage());
                if (attempt == 3) {
                    break;
                }
                Thread.sleep(1200L * attempt);
            }
        }
        throw lastException == null ? new IllegalStateException("Gemini request failed") : lastException;
    }

    private Map<String, Object> requestGeminiJsonOnce(String prompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s"
                .formatted(model, apiKey);
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "temperature", 0.35,
                        "maxOutputTokens", 4096,
                        "responseMimeType", "application/json"
                )
        );
        JsonNode root = restClient.post()
                .uri(url)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
        String text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        if (!StringUtils.hasText(text)) {
            throw new IllegalStateException("Gemini empty response");
        }
        String cleaned = text.replaceAll("^```json\\s*", "").replaceAll("^```\\s*", "").replaceAll("\\s*```$", "").trim();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            cleaned = cleaned.substring(start, end + 1);
        }
        return objectMapper.readValue(cleaned, new TypeReference<>() {});
    }

    private void validateGeminiKey() {
        if (!StringUtils.hasText(apiKey)) {
            throw new AppException(HttpStatus.SERVICE_UNAVAILABLE, "Gemini API 키가 설정되어 있지 않아 현재 AI 첨삭을 사용할 수 없습니다.");
        }
    }

    private String text(Map<String, Object> response, String key, String fallback) {
        Object value = response.get(key);
        return value == null || !StringUtils.hasText(value.toString()) ? fallback : value.toString();
    }

    private String textOrJoinedList(Map<String, Object> response, String textKey, String listKey, String fallback) {
        Object textValue = response.get(textKey);
        if (textValue != null && StringUtils.hasText(textValue.toString())) {
            return textValue.toString();
        }
        Object listValue = response.get(listKey);
        if (listValue instanceof List<?> list) {
            String joined = String.join("\n", list.stream().map(Object::toString).filter(StringUtils::hasText).toList());
            return StringUtils.hasText(joined) ? joined : fallback;
        }
        return fallback;
    }

    @SuppressWarnings("unchecked")
    private List<String> stringList(Map<String, Object> response, String key, List<String> fallback) {
        Object value = response.get(key);
        if (value instanceof List<?> list) {
            return list.stream().map(Object::toString).filter(StringUtils::hasText).toList();
        }
        if (value instanceof String string && StringUtils.hasText(string)) {
            return List.of(string);
        }
        if (value instanceof LinkedHashMap<?, ?> map) {
            return map.values().stream().map(Object::toString).filter(StringUtils::hasText).toList();
        }
        return fallback;
    }

    private String safe(String value) {
        return safe(value, "");
    }

    private String safe(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private static String limitLog(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.length() <= 800 ? value : value.substring(0, 800) + "...";
    }

    private static long resolveRetryDelayMillis(String responseBody, int attempt) {
        if (StringUtils.hasText(responseBody)) {
            Matcher matcher = GEMINI_RETRY_SECONDS.matcher(responseBody);
            if (matcher.find()) {
                double seconds = Double.parseDouble(matcher.group(1));
                return Math.min(65_000L, Math.max(2_000L, (long) ((seconds + 1.0) * 1000L)));
            }
        }
        return 1500L * attempt;
    }

    private record AiReviewContext(
            String userName,
            String introduction,
            String markdownContent,
            List<UserSkillResponse> skills,
            List<ProjectResponse> projects,
            List<MatchResponse> topMatches,
            List<String> missingSkills
    ) {
        String toPromptText() {
            return """
                    이름: %s
                    자기소개: %s
                    Markdown 본문: %s
                    보유 기술: %s
                    프로젝트: %s
                    매칭률 상위 공고: %s
                    반복 부족 기술: %s
                    """.formatted(
                    userName,
                    introduction,
                    markdownContent,
                    skills.stream().map(skill -> skill.name() + "(" + skill.level() + ")").toList(),
                    projects.stream().map(project -> Map.of(
                            "title", project.title(),
                            "description", safeStatic(project.description()),
                            "role", safeStatic(project.roleDescription()),
                            "skills", project.skills().stream().map(skill -> skill.name()).toList()
                    )).toList(),
                    topMatches.stream().map(match -> Map.of(
                            "title", match.jobTitle(),
                            "company", match.companyName(),
                            "rate", match.matchRate(),
                            "missing", match.missingSkills()
                    )).toList(),
                    missingSkills
            );
        }

        String toCompactPromptText() {
            return """
                    이름: %s
                    자기소개: %s
                    Markdown 본문 요약: %s
                    보유 기술: %s
                    대표 프로젝트: %s
                    반복 부족 기술: %s
                    """.formatted(
                    userName,
                    limit(introduction, 600),
                    limit(markdownContent, 1000),
                    skills.stream().limit(8).map(skill -> skill.name() + "(" + skill.level() + ")").toList(),
                    projects.stream().limit(4).map(project -> Map.of(
                            "title", project.title(),
                            "description", limit(safeStatic(project.description()), 220),
                            "role", limit(safeStatic(project.roleDescription()), 160),
                            "skills", project.skills().stream().limit(5).map(skill -> skill.name()).toList()
                    )).toList(),
                    missingSkills
            );
        }

        private static String safeStatic(String value) {
            return StringUtils.hasText(value) ? value : "";
        }

        private static String limit(String value, int max) {
            String safe = safeStatic(value);
            return safe.length() <= max ? safe : safe.substring(0, max) + "...";
        }
    }
}
