package com.sj.voicebook.member.dto.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailVerifyDto(
        @Email(message = "유효한 이메일 형식이어야 합니다")
        @NotBlank(message = "이메일은 필수입니다")
        String email,
        @NotBlank(message = "인증 코드는 필수입니다")
        String code
) {
}
