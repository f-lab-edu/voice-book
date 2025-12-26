package com.sj.voicebook.member.controller;

import com.sj.voicebook.global.ApiResponse;
import com.sj.voicebook.member.dto.api.LoginRequest;
import com.sj.voicebook.member.dto.api.LoginResponse;
import com.sj.voicebook.member.dto.api.RefreshTokenRequest;
import com.sj.voicebook.member.dto.api.RefreshTokenResponse;
import com.sj.voicebook.member.service.impl.AuthService;
import com.sj.voicebook.member.service.ImageService;
import com.sj.voicebook.member.service.SignUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/members")
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request.email(), request.password());
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "로그인에 성공했습니다."));
    }

    /**
     * 토큰 재발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(response, "토큰이 재발급되었습니다."));
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다."));
    }

}
