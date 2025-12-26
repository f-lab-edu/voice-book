package com.sj.voicebook.member.controller;

import com.sj.voicebook.global.ApiResponse;
import com.sj.voicebook.member.dto.api.SignUpRequest;
import com.sj.voicebook.member.dto.application.SignUpCommand;
import com.sj.voicebook.member.service.SignUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {
    private final SignUpService signUpService;
    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Long>> signUp(
            @Valid @ModelAttribute SignUpRequest request) {

        SignUpCommand command = SignUpCommand.builder()
                .email(request.email())
                .nickname(request.nickname())
                .password(request.password())
                .profileImage(request.profileImage()).build();

        long userId = signUpService.signUp(command);
        ApiResponse<Long> response = ApiResponse.success(userId, "회원가입이 완료되었습니다.");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
