package com.sj.voicebook.member.service.impl;

import com.sj.voicebook.global.exception.BusinessException;
import com.sj.voicebook.global.exception.ErrorCode;
import com.sj.voicebook.global.jwt.JwtTokenProvider;
import com.sj.voicebook.global.util.RedisUtil;
import com.sj.voicebook.member.domain.Member;
import com.sj.voicebook.member.dto.api.LoginResponse;
import com.sj.voicebook.member.dto.api.RefreshTokenResponse;
import com.sj.voicebook.member.dto.application.MemberAuthInfo;
import com.sj.voicebook.member.dto.application.MemberTokenInfo;
import com.sj.voicebook.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtil redisUtil;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";

    /**
     * 로그인 - 필요한 정보만 조회하여 인증 처리
     */
    @Transactional
    public LoginResponse login(String email, String password) {
        // 로그인에 필요한 정보만 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
                member.getUserId(),
                member.getEmail(),
                member.getRole().name()
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(member.getUserId());

        // RefreshToken을 Redis에 저장
        long refreshTokenExpirationMinutes = jwtTokenProvider.getRefreshTokenExpiration() / 60000;
        redisUtil.setDataExpire(
                REFRESH_TOKEN_PREFIX + member.getUserId(),
                refreshToken,
                refreshTokenExpirationMinutes
        );

        // 마지막 로그인 시간 업데이트
        member.updateLastLoginAt(LocalDateTime.now());


        return new LoginResponse(
                accessToken,
                refreshToken,
                member.getUserId(),
                member.getEmail(),
                member.getNickname(),
                member.getProfileImage()
        );
    }

    /**
     * 토큰 재발급 - 필요한 정보만 조회하여 토큰 생성
     */
    @Transactional
    public RefreshTokenResponse refresh(String refreshToken) {
        // RefreshToken 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // RefreshToken에서 userId 추출
        Long userId = jwtTokenProvider.getUserId(refreshToken);

        // Redis에서 저장된 RefreshToken 조회
        String savedRefreshToken = redisUtil.getData(REFRESH_TOKEN_PREFIX + userId);
        if (savedRefreshToken == null) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        // RefreshToken 일치 여부 확인
        if (!savedRefreshToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 토큰 재발급에 필요한 정보만 조회
        MemberTokenInfo memberTokenInfo = memberRepository.findTokenInfoById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 새로운 토큰 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(
                memberTokenInfo.userId(),
                memberTokenInfo.email(),
                memberTokenInfo.role().name()
        );

        String newRefreshToken = jwtTokenProvider.createRefreshToken(memberTokenInfo.userId());

        // 새로운 RefreshToken을 Redis에 저장
        long refreshTokenExpirationMinutes = jwtTokenProvider.getRefreshTokenExpiration() / 60000;
        redisUtil.setDataExpire(
                REFRESH_TOKEN_PREFIX + memberTokenInfo.userId(),
                newRefreshToken,
                refreshTokenExpirationMinutes
        );

        log.info("Token refreshed for user: {}", memberTokenInfo.email());

        return new RefreshTokenResponse(newAccessToken, newRefreshToken);
    }

    /**
     * 로그아웃 - Redis에서 RefreshToken 삭제
     */
    @Transactional
    public void logout(Long userId) {
        redisUtil.deleteData(REFRESH_TOKEN_PREFIX + userId);
        log.info("User logged out: {}", userId);
    }
}

