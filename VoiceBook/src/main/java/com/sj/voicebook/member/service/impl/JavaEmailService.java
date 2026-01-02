package com.sj.voicebook.member.service.impl;

import com.sj.voicebook.global.exception.BusinessException;
import com.sj.voicebook.global.exception.ErrorCode;
import com.sj.voicebook.global.util.RedisUtil;
import com.sj.voicebook.member.repository.MemberRepository;
import com.sj.voicebook.member.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class JavaEmailService implements EmailService {
    private final JavaMailSender javaMailSender;
    private final RedisUtil redisUtil;
    private final MemberRepository memberRepository;

    private final Executor emailExecutor;

    private static final String EMAIL_AUTH_PREFIX = "email:auth:";
    private static final long EXPIRE_MINUTES = 5;
    private static final String EMAIL_RATE_LIMIT_PREFIX = "email:rate:";
    private static final long RATE_LIMIT_SECONDS = 60;
    private static final int CODE_LENGTH = 6;
    private static final String EMAIL_ATTEMPT_PREFIX = "email:attempt:";
    private static final int MAX_ATTEMPTS = 5;

    @Value("${spring.mail.username}") // application.yml의 username 가져오기
    private String senderEmail;

    public JavaEmailService(JavaMailSender javaMailSender,
                            RedisUtil redisUtil,
                            MemberRepository memberRepository,
                            @Qualifier("emailExecutor")
    Executor emailExecutor) {
        this.javaMailSender = javaMailSender;
        this.redisUtil = redisUtil;
        this.memberRepository = memberRepository;
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
                    helper.setText(buildEmailContent(authCode), true);

                    javaMailSender.send(mimeMessage);
                } catch (MessagingException e) {
                    log.error("이메일 전송 실패: {}", toEmail, e);
                    redisUtil.deleteData(redisKey);
                }
            }, emailExecutor).exceptionally(ex -> {
                log.error("비동기 이메일 전송 중 예외 발생: {}", toEmail, ex);
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
    }

    /**
     * 6자리 랜덤 숫자 인증 코드 생성
     */
    private String createCode() {
        Random random = new Random();
        return String.format("%0" + CODE_LENGTH + "d", random.nextInt((int) Math.pow(10, CODE_LENGTH)));
    }

    /**
     * HTML 형식의 이메일 본문 생성 - Bootstrap 기반 반응형 디자인
     */
    private String buildEmailContent(String authCode) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta http-equiv="X-UA-Compatible" content="IE=edge">
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
                <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700&display=swap" rel="stylesheet">
                <style>
                    body {
                        font-family: 'Noto Sans KR', 'Malgun Gothic', Arial, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        padding: 20px;
                    }
                    .email-container {
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #ffffff;
                        border-radius: 16px;
                        overflow: hidden;
                        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
                    }
                    .email-header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        padding: 40px 20px;
                        text-align: center;
                        color: white;
                    }
                    .brand-name {
                        font-size: 32px;
                        font-weight: 700;
                        margin: 0;
                        letter-spacing: -0.5px;
                    }
                    .brand-subtitle {
                        font-size: 14px;
                        opacity: 0.95;
                        margin-top: 8px;
                    }
                    .email-body {
                        padding: 40px 30px;
                    }
                    .welcome-text {
                        font-size: 18px;
                        font-weight: 500;
                        color: #333;
                        margin-bottom: 12px;
                    }
                    .info-text {
                        color: #666;
                        font-size: 15px;
                        line-height: 1.6;
                        margin-bottom: 30px;
                    }
                    .code-container {
                        background: linear-gradient(135deg, #f5f7fa 0%%, #c3cfe2 100%%);
                        border: 2px dashed #667eea;
                        border-radius: 12px;
                        padding: 30px 20px;
                        text-align: center;
                        margin-bottom: 30px;
                    }
                    .code-label {
                        font-size: 13px;
                        color: #666;
                        margin-bottom: 10px;
                        font-weight: 500;
                    }
                    .code-number {
                        font-size: 42px;
                        font-weight: 700;
                        color: #667eea;
                        letter-spacing: 8px;
                        font-family: 'Courier New', monospace;
                    }
                    .alert-info {
                        background-color: #e7f3ff;
                        border-left: 4px solid #2196F3;
                        padding: 15px 20px;
                        border-radius: 8px;
                        margin-bottom: 15px;
                    }
                    .alert-info p {
                        margin: 0;
                        color: #1976D2;
                        font-size: 14px;
                    }
                    .alert-warning {
                        background-color: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 15px 20px;
                        border-radius: 8px;
                    }
                    .alert-warning p {
                        margin: 0;
                        color: #856404;
                        font-size: 13px;
                    }
                    .email-footer {
                        background-color: #f8f9fa;
                        padding: 25px 30px;
                        text-align: center;
                        border-top: 1px solid #e9ecef;
                    }
                    .footer-text {
                        color: #6c757d;
                        font-size: 13px;
                        margin: 5px 0;
                    }
                    .icon {
                        width: 50px;
                        height: 50px;
                        background-color: rgba(255, 255, 255, 0.2);
                        border-radius: 50%%;
                        display: inline-flex;
                        align-items: center;
                        justify-content: center;
                        margin-bottom: 15px;
                        font-size: 24px;
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <!-- Header -->
                    <div class="email-header">
                        <div class="icon">📚</div>
                        <h1 class="brand-name">말로쓴책</h1>
                        <p class="brand-subtitle">VoiceBook - 당신의 이야기를 책으로</p>
                    </div>
                    
                    <!-- Body -->
                    <div class="email-body">
                        <p class="welcome-text">안녕하세요! 👋</p>
                        <p class="info-text">
                            <strong>말로쓴책</strong> 회원가입을 위한 이메일 인증 단계입니다.<br>
                            아래 인증 코드를 입력하여 본인 인증을 완료해주세요.
                        </p>
                        
                        <!-- Code Box -->
                        <div class="code-container">
                            <div class="code-label">인증 코드</div>
                            <div class="code-number">%s</div>
                        </div>
                        
                        <!-- Alert Info -->
                        <div class="alert-info">
                            <p>⏰ 인증 코드는 <strong>%d분</strong>간 유효합니다.</p>
                        </div>
                        
                        <div class="alert-warning">
                            <p>⚠️ 본인이 요청하지 않았다면 이 이메일을 무시하셔도 됩니다.</p>
                        </div>
                    </div>
                    
                    <!-- Footer -->
                    <div class="email-footer">
                        <p class="footer-text">이 메일은 발신 전용입니다.</p>
                        <p class="footer-text">© 2025 말로쓴책 (VoiceBook). All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, authCode, EXPIRE_MINUTES);
    }
}
