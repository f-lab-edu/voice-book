package com.sj.voicebook.global.util;

import com.sj.voicebook.member.service.provider.EmailTemplateProvider;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Component
public class CustomMailSender {
    private final JavaMailSender javaMailSender;
    private final RedisUtil redisUtil;
    private final EmailTemplateProvider emailTemplateProvider;
    private final Executor emailExecutor;


    @Value("${spring.mail.username}") // application.yml의 username 가져오기
    private String senderEmail;

    public CustomMailSender(JavaMailSender javaMailSender, RedisUtil redisUtil, EmailTemplateProvider emailTemplateProvider, Executor emailExecutor) {
        this.javaMailSender = javaMailSender;
        this.redisUtil = redisUtil;
        this.emailTemplateProvider = emailTemplateProvider;
        this.emailExecutor = emailExecutor;
    }

    public void sendEmailValidateAsync(String toEmail, String authCode, String redisKey, String rateLimitKey, long expireMinutes) {
        CompletableFuture.runAsync(() -> {
            try {
                // [통합된 로직] MimeMessage 생성 및 설정
                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

                helper.setTo(toEmail);
                helper.setFrom(senderEmail);
                helper.setSubject("[말로쓴책] 이메일 인증 코드입니다.");
                helper.setText(emailTemplateProvider.buildAuthCodeEmail(authCode, expireMinutes), true);

                // 이메일 전송
                javaMailSender.send(mimeMessage);

            } catch (MessagingException e) {
                log.error("이메일 전송 실패: {}", toEmail, e);
                cleanupOnFailure(redisKey, rateLimitKey);
            }
        }, emailExecutor).exceptionally(ex -> {
            log.error("비동기 이메일 전송 중 예외 발생: {}", toEmail, ex);
            cleanupOnFailure(redisKey, rateLimitKey);
            return null;
        });
    }

    // 참고: 중복 코드 방지를 위해 유지
    private void cleanupOnFailure(String redisKey, String rateLimitKey) {
        redisUtil.deleteData(redisKey);
        redisUtil.deleteData(rateLimitKey);
    }
}
