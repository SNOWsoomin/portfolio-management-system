package com.example.portfolio.dto;

import java.util.List;

public class AiReviewDtos {
    public record AiReviewRequest(String introduction, String markdownContent) {}

    public record AiDiagnosisResponse(
            String provider,
            boolean fallback,
            String diagnosis,
            List<String> problems,
            List<String> strengths,
            List<String> suggestedFocus,
            String model
    ) {}

    public record AiPolishResponse(
            String provider,
            boolean fallback,
            String diagnosis,
            String improvedIntroduction,
            String improvedMarkdownContent,
            List<String> changeSummary,
            List<String> warnings,
            String model
    ) {}

    public record AiFullReviewResponse(
            String provider,
            boolean fallback,
            String diagnosis,
            List<String> problems,
            List<String> strengths,
            List<String> suggestedFocus,
            String improvedIntroduction,
            String improvedMarkdownContent,
            List<String> changeSummary,
            List<String> warnings,
            String model
    ) {}
}
