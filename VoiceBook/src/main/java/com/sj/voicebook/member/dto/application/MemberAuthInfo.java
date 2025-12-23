package com.sj.voicebook.member.dto.application;

import com.sj.voicebook.global.Role;

/**
 * 로그인 인증에 필요한 회원 정보
 */
public record MemberAuthInfo(Long userId, String email, String password, String nickname, String profileImage,
                             Role role) {
}

