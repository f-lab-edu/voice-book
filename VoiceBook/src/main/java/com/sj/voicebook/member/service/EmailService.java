package com.sj.voicebook.member.service;

public interface EmailService {
    void sendEmail(String email);
    void verifyEmailCode(String email, String code);
}
