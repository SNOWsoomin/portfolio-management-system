package com.example.portfolio.controller;

import com.example.portfolio.dto.ApiResponse;
import com.example.portfolio.dto.Requests.LoginRequest;
import com.example.portfolio.dto.Requests.SignupRequest;
import com.example.portfolio.dto.Responses.LoginResponse;
import com.example.portfolio.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ApiResponse<Void> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ApiResponse.ok(null, "회원가입이 완료되었습니다.");
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request), "로그인에 성공했습니다.");
    }
}
