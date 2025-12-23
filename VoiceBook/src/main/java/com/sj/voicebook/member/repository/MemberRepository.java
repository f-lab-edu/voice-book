package com.sj.voicebook.member.repository;

import com.sj.voicebook.member.domain.Member;
import com.sj.voicebook.member.dto.application.MemberAuthInfo;
import com.sj.voicebook.member.dto.application.MemberTokenInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<Member> findByEmail(String email);

    /**
     * 로그인 인증에 필요한 회원 정보만 조회
     */
    @Query("SELECT new com.sj.voicebook.member.dto.application.MemberAuthInfo(" +
            "m.userId, m.email, m.password, m.nickname, m.profileImage, m.role) " +
            "FROM Member m WHERE m.email = :email")
    Optional<MemberAuthInfo> findAuthInfoByEmail(@Param("email") String email);

    /**
     * 토큰 재발급에 필요한 회원 정보만 조회
     */
    @Query("SELECT new com.sj.voicebook.member.dto.application.MemberTokenInfo(" +
            "m.userId, m.email, m.role) " +
            "FROM Member m WHERE m.userId = :userId")
    Optional<MemberTokenInfo> findTokenInfoById(@Param("userId") Long userId);
}
