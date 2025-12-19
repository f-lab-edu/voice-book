package com.sj.voicebook.member.dto.application;

import com.sj.voicebook.global.MemberStatus;
import com.sj.voicebook.global.Role;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;

import java.time.LocalDateTime;

public record CreateUserCommand(
        String email,
        String encryptedPassword,
        String nickname,
        String profileImage) {

}
