package com.sj.voicebook.member.service.impl;

import com.sj.voicebook.global.exception.BusinessException;
import com.sj.voicebook.global.exception.ErrorCode;
import com.sj.voicebook.member.domain.Member;
import com.sj.voicebook.member.dto.api.SignUpRequest;
import com.sj.voicebook.member.repository.MemberRepository;
import com.sj.voicebook.member.service.EmailService;
import com.sj.voicebook.member.service.ImageService;
import com.sj.voicebook.member.service.validator.EmailDuplicationValidator;
import com.sj.voicebook.member.service.SignUpService;
import com.sj.voicebook.member.service.validator.NicknameDuplicationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignUpServiceImpl implements SignUpService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailDuplicationValidator emailDuplicationValidator;
    private final NicknameDuplicationValidator nicknameDuplicationValidator;
    private final ImageService imageService;
    private final TransactionTemplate transactionTemplate;
    private final EmailService emailService;

    @Override
    public Long signUp(SignUpRequest request) {
        // 이메일 중복 검사
        emailDuplicationValidator.validate(request.email());

        // 닉네임 중복 검사
        nicknameDuplicationValidator.validate(request.nickname());

        // 이메일 인증 검사
        if(!emailService.isEmailVerified(request.email())){
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }



        String profileImageKey = resolveProfileImageKey(request);

        Member member = Member.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname(),
                profileImageKey
        );
        return transactionTemplate.execute(
                status -> memberRepository.save(member).getUserId()
        );
    }

    private String resolveProfileImageKey(SignUpRequest request) {
        if (request.profileImage() != null && !request.profileImage().isEmpty()) {
            return imageService.upload(request.profileImage());
        } else {
            return imageService.getBasicProfileImageKey();
        }
    }



    @Transactional(readOnly = true)
    @Override
    public Boolean checkEmailDuplication(String email) {
        return emailDuplicationValidator.checkExists(email);
    }

    @Transactional(readOnly = true)
    @Override
    public Boolean checkNicknameDuplication(String nickname) {
        return nicknameDuplicationValidator.checkExists(nickname);
    }
}
