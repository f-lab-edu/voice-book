package com.sj.voicebook.member.dto.application;
import com.sj.voicebook.global.Role;
import lombok.AllArgsConstructor;

/**
 * 토큰 재발급에 필요한 회원 정보
 */
public record MemberTokenInfo(Long userId, String email, Role role) {
}
