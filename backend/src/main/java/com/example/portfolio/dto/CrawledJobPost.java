package com.example.portfolio.dto;

import java.util.List;

public record CrawledJobPost(
        String externalId,
        String title,
        String companyName,
        String position,
        String description,
        String sourceUrl,
        List<String> skillNames
) {}
