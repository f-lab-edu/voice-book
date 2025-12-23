package com.sj.voicebook.member.controller;

import com.sj.voicebook.global.ApiResponse;
import com.sj.voicebook.member.dto.api.EmailRequestDto;
import com.sj.voicebook.member.dto.api.EmailVerifyDto;
import com.sj.voicebook.member.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/email")
@Slf4j
public class EmailController {
    private final EmailService emailService;

    /**
     * 인증코드 전송 요청
     * @param request 이메일 주소
     * @return 성공 메시지
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendEmail(@Valid @RequestBody EmailRequestDto request) {
        emailService.sendEmail(request.email());
        return ResponseEntity.ok(ApiResponse.success("인증 코드가 발송되었습니다."));
    }

    /**
     * 인증코드 검증 요청
     * @param request 이메일 주소와 인증 코드
     * @return 성공 메시지
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody EmailVerifyDto request) {
        emailService.verifyEmailCode(request.email(), request.code());
        return ResponseEntity.ok(ApiResponse.success("이메일 인증에 성공했습니다."));
    }
}
