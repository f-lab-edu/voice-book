package com.sj.voicebook.member.dto.application;

import lombok.Builder;

@Builder
public record CreateUserCommand(
        String email,
        String password,
        String nickname,
        String profileImage) {

}
