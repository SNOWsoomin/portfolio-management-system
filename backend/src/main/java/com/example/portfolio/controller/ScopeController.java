package com.example.portfolio.controller;

import com.example.portfolio.dto.ApiResponse;
import com.example.portfolio.dto.Responses.ScopeStatusResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ScopeController {
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("ok", "백엔드 모듈이 정상 실행 중입니다.");
    }

    @GetMapping("/dev/scope")
    public ApiResponse<ScopeStatusResponse> scope() {
        return ApiResponse.ok(new ScopeStatusResponse(
                "backend-johyeonmin-scope",
                "조현민 / 백엔드",
                "week2-baseline-ready",
                List.of(
                        "Spring Boot 초기 세팅",
                        "H2 DB 연동",
                        "JPA Entity/Repository 구성",
                        "Spring Security + JWT 로그인 인증",
                        "USER / ADMIN 권한 분리",
                        "기초 seed 데이터 구성"
                ),
                List.of(
                        "포트폴리오 CRUD API 연결",
                        "프로젝트 경험 CRUD API 연결",
                        "채용공고 관리 API 연결",
                        "프론트엔드 화면 연동"
                )
        ));
    }
}
