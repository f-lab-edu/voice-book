package com.sj.voicebook.member.controller;

import com.sj.voicebook.member.dto.api.CreateUserRequest;
import com.sj.voicebook.member.dto.application.CreateUserCommand;
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
public class MemberController {
    private final SignUpService signUpService;

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@Valid@ModelAttribute CreateUserRequest request,
                                         @RequestParam(value = "profileImage", required = false)
                                         MultipartFile profileImage ){
        CreateUserCommand command = CreateUserCommand.builder()
                .email(request.email())
                .nickname(request.nickname())
                .password(request.password())
                .profileImage("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSTj6s5jkrtoGn-lV4NKewUhS38dDDWRnmv-A&s")
                .build();


        return ResponseEntity.status(HttpStatus.CREATED).body(signUpService.signUp(command));



    }


}
