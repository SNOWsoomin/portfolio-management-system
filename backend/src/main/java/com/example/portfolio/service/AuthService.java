package com.example.portfolio.service;

import com.example.portfolio.dto.Requests.LoginRequest;
import com.example.portfolio.dto.Requests.SignupRequest;
import com.example.portfolio.dto.Responses.LoginResponse;
import com.example.portfolio.entity.Role;
import com.example.portfolio.entity.User;
import com.example.portfolio.exception.AppException;
import com.example.portfolio.repository.UserRepository;
import com.example.portfolio.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new AppException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setName(request.name());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "로그인 정보를 확인해주세요."));
        String token = jwtUtil.createToken(user.getEmail(), user.getRole().name());
        return new LoginResponse(token, user.getName(), user.getRole().name());
    }
}
