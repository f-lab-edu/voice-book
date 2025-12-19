package com.sj.voicebook.global.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE("C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED("C002", "허용되지 않은 HTTP 메소드입니다."),
    INTERNAL_SERVER_ERROR("C003", "서버 내부 오류가 발생했습니다."),
    INVALID_TYPE_VALUE("C004", "잘못된 타입입니다."),
    ACCESS_DENIED("C005", "접근이 거부되었습니다."),

    // Member
    EMAIL_DUPLICATION("M001", "이미 사용 중인 이메일입니다."),
    NICKNAME_DUPLICATION("M002", "이미 사용 중인 닉네임입니다."),
    MEMBER_NOT_FOUND("M003", "회원을 찾을 수 없습니다."),
    INVALID_PASSWORD("M004", "비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED_MEMBER("M005", "인증되지 않은 회원입니다."),

    // Auth
    INVALID_TOKEN("A001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN("A002", "만료된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND("A003", "리프레시 토큰을 찾을 수 없습니다.");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}

