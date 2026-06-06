package com.example.portfolio.service;

import com.example.portfolio.dto.CrawledJobPost;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

@Service
public class JobKoreaCrawlerService {
    private static final String SOURCE_NAME = "JobKorea";
    private static final String SEARCH_URL = "https://www.jobkorea.co.kr/Search/?stext=%s&tabType=recruit&Page_No=%d";
    private static final Pattern JOB_ID_PATTERN = Pattern.compile("/Recruit/GI_Read/(\\d+)");
    private static final Pattern DETAIL_LINK_PATTERN = Pattern.compile("(?:https://www\\.jobkorea\\.co\\.kr)?/Recruit/GI_Read/\\d+[^\\\"'<\\s]*");

    private static final Map<String, String> SKILL_CATEGORIES = new LinkedHashMap<>();
    static {
        SKILL_CATEGORIES.put("Java", "Backend");
        SKILL_CATEGORIES.put("Spring Boot", "Backend");
        SKILL_CATEGORIES.put("Spring", "Backend");
        SKILL_CATEGORIES.put("JPA", "Backend");
        SKILL_CATEGORIES.put("Spring Security", "Backend");
        SKILL_CATEGORIES.put("Node.js", "Backend");
        SKILL_CATEGORIES.put("Python", "Backend");
        SKILL_CATEGORIES.put("React", "Frontend");
        SKILL_CATEGORIES.put("JavaScript", "Frontend");
        SKILL_CATEGORIES.put("TypeScript", "Frontend");
        SKILL_CATEGORIES.put("Next.js", "Frontend");
        SKILL_CATEGORIES.put("Vue", "Frontend");
        SKILL_CATEGORIES.put("HTML", "Frontend");
        SKILL_CATEGORIES.put("CSS", "Frontend");
        SKILL_CATEGORIES.put("MySQL", "Database");
        SKILL_CATEGORIES.put("PostgreSQL", "Database");
        SKILL_CATEGORIES.put("Oracle", "Database");
        SKILL_CATEGORIES.put("MongoDB", "Database");
        SKILL_CATEGORIES.put("Redis", "Database");
        SKILL_CATEGORIES.put("AWS", "DevOps");
        SKILL_CATEGORIES.put("Docker", "DevOps");
        SKILL_CATEGORIES.put("Kubernetes", "DevOps");
        SKILL_CATEGORIES.put("Linux", "DevOps");
        SKILL_CATEGORIES.put("GitHub", "Tool");
        SKILL_CATEGORIES.put("Git", "Tool");
    }

    public List<CrawledJobPost> crawlDeveloperJobs(String keyword, int limit) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? "개발자" : keyword.trim();
        int safeLimit = Math.max(1, Math.min(limit, 60));
        List<String> keywords = expandDeveloperKeywords(normalizedKeyword);
        LinkedHashMap<String, CrawledJobPost> collected = new LinkedHashMap<>();

        for (String currentKeyword : keywords) {
            for (int page = 1; page <= 3 && collected.size() < safeLimit; page++) {
                String url = SEARCH_URL.formatted(URLEncoder.encode(currentKeyword, StandardCharsets.UTF_8), page);
                try {
                    Document document = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/124 Safari/537.36")
                            .referrer("https://www.jobkorea.co.kr/")
                            .timeout(12_000)
                            .get();
                    List<CrawledJobPost> parsed = parseCards(document, safeLimit - collected.size());
                    if (parsed.isEmpty()) {
                        parsed = parseLinksFallback(document.html(), safeLimit - collected.size());
                    }
                    for (CrawledJobPost job : parsed) {
                        if (!job.externalId().isBlank()) {
                            collected.putIfAbsent(job.externalId(), job);
                        }
                        if (collected.size() >= safeLimit) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    if (collected.isEmpty() && currentKeyword.equals(keywords.get(0)) && page == 1) {
                        throw new IllegalStateException("잡코리아 공고 수집에 실패했습니다. 잠시 후 다시 시도해주세요.", e);
                    }
                }
            }
        }

        return new ArrayList<>(collected.values());
    }

    public String sourceName() {
        return SOURCE_NAME;
    }

    public static String categoryOf(String skillName) {
        return SKILL_CATEGORIES.getOrDefault(skillName, "Tool");
    }

    private List<String> expandDeveloperKeywords(String keyword) {
        LinkedHashSet<String> keywords = new LinkedHashSet<>();
        keywords.add(keyword);
        if (keyword.contains("개발")) {
            keywords.add("백엔드 개발자");
            keywords.add("프론트엔드 개발자");
            keywords.add("웹 개발자");
            keywords.add("Spring 개발자");
            keywords.add("React 개발자");
            keywords.add("Java 개발자");
        }
        return new ArrayList<>(keywords);
    }

    private List<CrawledJobPost> parseCards(Document document, int limit) {
        List<CrawledJobPost> jobs = new ArrayList<>();
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        for (Element card : document.select("[data-sentry-component=CardJob]")) {
            Element link = card.selectFirst("a[href*=/Recruit/GI_Read/]");
            if (link == null) {
                continue;
            }

            String sourceUrl = normalizeUrl(link.attr("href"));
            String externalId = extractExternalId(sourceUrl);
            if (externalId.isBlank() || !seen.add(externalId)) {
                continue;
            }

            String title = firstText(card.select("[data-sentry-component=Title] span"), link.text());
            String company = extractCompany(card);
            String position = extractPosition(card.text(), title);
            String description = cleanText(card.text());
            List<String> skills = mergeSkills(detectSkills(title + " " + description), inferSkills(title + " " + position + " " + description));

            jobs.add(new CrawledJobPost(externalId, title, company, position, description, sourceUrl, skills));
            if (jobs.size() >= limit) {
                break;
            }
        }
        return jobs;
    }

    private List<CrawledJobPost> parseLinksFallback(String html, int limit) {
        List<CrawledJobPost> jobs = new ArrayList<>();
        LinkedHashSet<String> links = new LinkedHashSet<>();
        Matcher matcher = DETAIL_LINK_PATTERN.matcher(html);
        while (matcher.find() && links.size() < limit) {
            links.add(normalizeUrl(matcher.group()));
        }

        for (String sourceUrl : links) {
            String externalId = extractExternalId(sourceUrl);
            jobs.add(new CrawledJobPost(
                    externalId,
                    "잡코리아 개발자 채용공고 " + externalId,
                    "잡코리아 등록 기업",
                    "개발자",
                    "잡코리아 검색 결과에서 수집한 개발자 채용공고입니다.",
                    sourceUrl,
                    List.of("Java", "Spring Boot", "React")
            ));
        }
        return jobs;
    }

    private String extractCompany(Element card) {
        Element companyElement = card.selectFirst("span.mb-5 span.truncate");
        if (companyElement != null && !companyElement.text().isBlank()) {
            return companyElement.text().trim();
        }

        for (Element candidate : card.select("span.truncate")) {
            String text = candidate.text().trim();
            if (!text.isBlank() && !text.contains("개발자") && !text.contains("엔지니어") && text.length() <= 40) {
                return text;
            }
        }
        return "잡코리아 등록 기업";
    }

    private String extractPosition(String text, String title) {
        String target = title + " " + text;
        if (target.contains("백엔드") || target.toLowerCase().contains("backend")) {
            return "백엔드 개발자";
        }
        if (target.contains("프론트엔드") || target.toLowerCase().contains("frontend")) {
            return "프론트엔드 개발자";
        }
        if (target.contains("풀스택") || target.toLowerCase().contains("full")) {
            return "풀스택 개발자";
        }
        if (target.contains("앱") || target.contains("Android") || target.contains("iOS")) {
            return "앱 개발자";
        }
        return "개발자";
    }

    private List<String> detectSkills(String text) {
        String lower = text.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String skill : SKILL_CATEGORIES.keySet()) {
            String key = skill.toLowerCase();
            boolean matched = lower.contains(key)
                    || ("Spring Boot".equals(skill) && lower.contains("springboot"))
                    || ("Node.js".equals(skill) && lower.contains("nodejs"))
                    || ("Next.js".equals(skill) && lower.contains("nextjs"))
                    || ("JavaScript".equals(skill) && lower.contains("javascript"))
                    || ("TypeScript".equals(skill) && lower.contains("typescript"));
            if (matched) {
                result.add(skill);
            }
        }
        return result;
    }

    private List<String> inferSkills(String text) {
        String lower = text.toLowerCase();
        if (text.contains("백엔드") || lower.contains("backend") || text.contains("서버")) {
            return List.of("Java", "Spring Boot", "JPA", "MySQL");
        }
        if (lower.contains("sre") || lower.contains("cloud") || text.contains("클라우드")) {
            return List.of("AWS", "Docker", "Linux");
        }
        if (text.contains("프론트") || lower.contains("frontend")) {
            return List.of("React", "JavaScript", "HTML", "CSS");
        }
        if (text.contains("풀스택") || lower.contains("full")) {
            return List.of("Java", "Spring Boot", "React", "MySQL");
        }
        return List.of("Java", "JavaScript", "Git");
    }

    private List<String> mergeSkills(List<String> detected, List<String> inferred) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        if (detected != null) {
            merged.addAll(detected);
        }
        if (inferred != null) {
            merged.addAll(inferred);
        }
        return new ArrayList<>(merged);
    }

    private String firstText(Iterable<Element> elements, String fallback) {
        for (Element element : elements) {
            String text = element.text().trim();
            if (!text.isBlank()) {
                return text;
            }
        }
        return fallback == null || fallback.isBlank() ? "잡코리아 개발자 채용공고" : fallback.trim();
    }

    private String normalizeUrl(String url) {
        String cleaned = Jsoup.clean(url, Safelist.none())
                .replace("&amp;", "&")
                .replaceAll("\\s+", "")
                .trim();
        if (cleaned.startsWith("//")) {
            return "https:" + cleaned;
        }
        if (cleaned.startsWith("/")) {
            return "https://www.jobkorea.co.kr" + cleaned;
        }
        return cleaned;
    }

    private String extractExternalId(String sourceUrl) {
        Matcher matcher = JOB_ID_PATTERN.matcher(sourceUrl);
        return matcher.find() ? matcher.group(1) : "";
    }

    private String cleanText(String text) {
        return text == null ? "" : text.replaceAll("\\s+", " ").trim();
    }
}
