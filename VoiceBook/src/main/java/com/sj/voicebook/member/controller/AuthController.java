package com.sj.voicebook.member.controller;

import com.sj.voicebook.global.ApiResponse;
import com.sj.voicebook.member.dto.api.CreateUserRequest;
import com.sj.voicebook.member.dto.application.CreateUserCommand;
import com.sj.voicebook.member.service.ImageService;
import com.sj.voicebook.member.service.SignUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/members")
public class AuthController {
    private final SignUpService signUpService;
    private final ImageService imageService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signUp(
            @Valid @ModelAttribute CreateUserRequest request,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        String Key = imageService.upload(profileImage);

        CreateUserCommand command = CreateUserCommand.builder()
                .email(request.email())
                .nickname(request.nickname())
                .password(request.password())
                .profileImage(Key)
                .build();

        String result = signUpService.signUp(command);
        ApiResponse<String> response = ApiResponse.success(result, "회원가입이 완료되었습니다.");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }




}
