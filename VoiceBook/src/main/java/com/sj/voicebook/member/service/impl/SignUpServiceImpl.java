package com.sj.voicebook.member.service.impl;

import com.sj.voicebook.member.domain.Member;
import com.sj.voicebook.member.dto.application.CreateUserCommand;
import com.sj.voicebook.member.repository.MemberRepository;
import com.sj.voicebook.member.service.SignUpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignUpServiceImpl implements SignUpService {
    private final MemberRepository memberRepository;

    @Override
    public String signUp(CreateUserCommand command) {
        Member member = Member.create(
                command.email(),
                command.password(),
                command.nickname(),
                command.profileImage()
        );
        Member saveMember = memberRepository.save(member);
        return saveMember.getNickname()+"님 회원가입을 환영합니다!";
    }

    @Override
    public Boolean checkEmailDuplication(String email) {
        return null;
    }

    @Override
    public Boolean checkNicknameDuplication(String nickname) {
        return null;
    }
}
