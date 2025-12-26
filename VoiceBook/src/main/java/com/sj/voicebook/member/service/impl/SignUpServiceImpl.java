package com.sj.voicebook.member.service.impl;

import com.sj.voicebook.global.exception.BusinessException;
import com.sj.voicebook.global.exception.ErrorCode;
import com.sj.voicebook.member.domain.Member;
import com.sj.voicebook.member.dto.application.CreateUserCommand;
import com.sj.voicebook.member.repository.MemberRepository;
import com.sj.voicebook.member.service.validator.EmailDuplicationValidator;
import com.sj.voicebook.member.service.SignUpService;
import com.sj.voicebook.member.service.validator.NicknameDuplicatationValidtor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignUpServiceImpl implements SignUpService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailDuplicationValidator emailDuplicationValidator;
    private final NicknameDuplicatationValidtor nicknameDuplicatationValidtor;

    @Override
    @Transactional
    public String signUp(CreateUserCommand command) {
        // 이메일 중복 검사
        if (emailDuplicationValidator.isEmailDuplicated(command.email())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATION);
        }

        // 닉네임 중복 검사
        if (nicknameDuplicatationValidtor.isNicknameDuplicated(command.nickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_DUPLICATION);
        }

        Member member = Member.create(
                command.email(),
                passwordEncoder.encode(command.password()),
                command.nickname(),
                command.profileImage()
        );
        Member saveMember = memberRepository.save(member);
        return saveMember.getNickname() + "님 회원가입을 환영합니다!";
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean checkEmailDuplication(String email) {
        return emailDuplicationValidator.isEmailDuplicated(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean checkNicknameDuplication(String nickname) {
        return nicknameDuplicatationValidtor.isNicknameDuplicated(nickname);
    }
}
