package com.sj.voicebook.member.dto.api;

public record LoginResponse(String accessToken, String refreshToken, Long userId, String email, String nickname,
                            String profileImage) {
}
