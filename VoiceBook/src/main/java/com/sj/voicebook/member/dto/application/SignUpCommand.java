package com.sj.voicebook.member.dto.application;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record SignUpCommand(
        String email,
        String password,
        String nickname,
        MultipartFile profileImage) {

}
