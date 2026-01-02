package com.sj.voicebook.member.service.impl;

import com.sj.voicebook.global.exception.BusinessException;
import com.sj.voicebook.global.exception.ErrorCode;
import com.sj.voicebook.global.util.RedisUtil;
import com.sj.voicebook.member.repository.MemberRepository;
import com.sj.voicebook.member.service.EmailService;
import com.sj.voicebook.member.service.provider.EmailTemplateProvider;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class JavaEmailService implements EmailService {
    private final JavaMailSender javaMailSender;
    private final RedisUtil redisUtil;
    private final MemberRepository memberRepository;
    private final EmailTemplateProvider emailTemplateProvider;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final Executor emailExecutor;

    private static final String EMAIL_AUTH_PREFIX = "email:auth:";
    private static final long EXPIRE_MINUTES = 5;
    private static final String EMAIL_RATE_LIMIT_PREFIX = "email:rate:";
    private static final long RATE_LIMIT_SECONDS = 60;
    private static final int CODE_LENGTH = 6;
    private static final String EMAIL_ATTEMPT_PREFIX = "email:attempt:";
    private static final int MAX_ATTEMPTS = 5;
    private static final String EMAIL_VERIFIED_PREFIX = "email:verified:";
    private static final long VERIFIED_EXPIRE_MINUTES = 30;

    @Value("${spring.mail.username}") // application.yml의 username 가져오기
    private String senderEmail;

    public JavaEmailService(JavaMailSender javaMailSender,
                            RedisUtil redisUtil,
                            MemberRepository memberRepository, EmailTemplateProvider emailTemplateProvider,
                            @Qualifier("emailExecutor")
    Executor emailExecutor) {
        this.javaMailSender = javaMailSender;
        this.redisUtil = redisUtil;
        this.memberRepository = memberRepository;
        this.emailTemplateProvider = emailTemplateProvider;
        this.emailExecutor = emailExecutor;
    }


    @Override
        public void sendEmail(String toEmail) {
        if(memberRepository.existsByEmail(toEmail)) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATION);
        }

        String rateLimitKey = EMAIL_RATE_LIMIT_PREFIX + toEmail;

        // 이전 요청이 1분 이내에 있었는지 확인
        if (redisUtil.getData(rateLimitKey) != null) {
            throw new BusinessException(ErrorCode.EMAIL_SEND_TOO_FREQUENT);
        }

        // Redis에 저장할 키 생성
        String redisKey = EMAIL_AUTH_PREFIX + toEmail;

        // 기존 인증 코드가 있으면 삭제
            redisUtil.deleteData(redisKey);

        // 인증 코드 생성 및 Redis에 저장
        String authCode = createCode();
        // 인증 코드와 만료 시간 설정
        redisUtil.setDataExpire(redisKey, authCode, EXPIRE_MINUTES);
        // 1분 간격 제한 설정
        redisUtil.setDataExpireSeconds(rateLimitKey, "1", RATE_LIMIT_SECONDS);

            // CompletableFuture로 비동기 이메일 전송
            CompletableFuture.runAsync(() -> {
                try {
                    MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

                    helper.setTo(toEmail);
                    helper.setFrom(senderEmail);
                    helper.setSubject("[말로쓴책] 이메일 인증 코드입니다.");
                    helper.setText(emailTemplateProvider.buildAuthCodeEmail(authCode, EXPIRE_MINUTES), true);

                    javaMailSender.send(mimeMessage);
                } catch (MessagingException e) {
                    log.error("이메일 전송 실패: {}", toEmail, e);
                    redisUtil.deleteData(redisKey);
                    redisUtil.deleteData(rateLimitKey);
                }
            }, emailExecutor).exceptionally(ex -> {
                log.error("비동기 이메일 전송 중 예외 발생: {}", toEmail, ex);
                redisUtil.deleteData(redisKey);
                redisUtil.deleteData(rateLimitKey);
                return null;
            });
        }





    @Override
    public void verifyEmailCode(String email, String code) {
        // 시도 횟수 확인
        String attemptKey = EMAIL_ATTEMPT_PREFIX + email;
        String attempts = redisUtil.getData(attemptKey);

        if (attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPTS) {
            throw new BusinessException(ErrorCode.EMAIL_VERIFY_BLOCKED);
        }

        // Redis에서 인증 코드 조회
        String redisKey = EMAIL_AUTH_PREFIX + email;
        String codeInRedis = redisUtil.getData(redisKey);

        // 인증 코드 만료 여부 확인
        if (codeInRedis == null) {
            throw new BusinessException(ErrorCode.EMAIL_CODE_EXPIRED);
        }

        // 인증 코드 일치 여부 확인
        if (!codeInRedis.equals(code)) {
            redisUtil.increment(attemptKey, EXPIRE_MINUTES);
            throw new BusinessException(ErrorCode.EMAIL_CODE_MISMATCH);
        }

        // 인증 성공 시 Redis에서 인증 코드 및 시도 횟수 삭제
        redisUtil.deleteData(redisKey);
        redisUtil.deleteData(attemptKey);

        redisUtil.setDataExpire(EMAIL_VERIFIED_PREFIX+email, "true", VERIFIED_EXPIRE_MINUTES);
    }

    @Override
    public boolean isEmailVerified(String email) {
        return redisUtil.getData(EMAIL_VERIFIED_PREFIX+email)!=null;
    }

    /**
     * 6자리 랜덤 숫자 인증 코드 생성
     */
    private String createCode() {
        return String.format("%0" + CODE_LENGTH + "d", SECURE_RANDOM.nextInt((int) Math.pow(10, CODE_LENGTH)));
    }


}
