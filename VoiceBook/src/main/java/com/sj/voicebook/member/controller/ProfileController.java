package com.sj.voicebook.member.controller;

import com.sj.voicebook.global.ApiResponse;
import com.sj.voicebook.member.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/members")
public class ProfileController {
    private final ImageService imageService;

    @GetMapping("/profile-image")
    public ResponseEntity<ApiResponse<String>> viewImage(@RequestParam("key") String key) {
        String url = imageService.getImageUrl(key);
        ApiResponse<String> response = ApiResponse.success(url, "프로필 이미지 조회에 성공했습니다.");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
