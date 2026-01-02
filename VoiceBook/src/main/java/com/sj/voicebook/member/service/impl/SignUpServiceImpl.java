package com.sj.voicebook.member.service.impl;

import com.sj.voicebook.member.domain.Member;
import com.sj.voicebook.member.dto.api.SignUpRequest;
import com.sj.voicebook.member.repository.MemberRepository;
import com.sj.voicebook.member.service.ImageService;
import com.sj.voicebook.member.service.validator.EmailDuplicationValidator;
import com.sj.voicebook.member.service.SignUpService;
import com.sj.voicebook.member.service.validator.NicknameDuplicationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SignUpServiceImpl implements SignUpService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailDuplicationValidator emailDuplicationValidator;
    private final NicknameDuplicationValidator nicknameDuplicationValidator;
    private final ImageService imageService;

    @Override
    @Transactional
    public Long signUp(SignUpRequest request) {
        // 이메일 중복 검사
        emailDuplicationValidator.validate(request.email());

        // 닉네임 중복 검사
        nicknameDuplicationValidator.validate(request.nickname());

        String imageUrl;

        if (request.profileImage() != null && !request.profileImage().isEmpty()) {
            imageUrl = imageService.upload(request.profileImage());
        }else {
            imageUrl = imageService.getBasicProfileImageKey();
        }

        Member member = Member.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname(),
                imageUrl
        );
        Member saveMember = memberRepository.save(member);
        return saveMember.getUserId();
    }

    @Override
    public Boolean checkEmailDuplication(String email) {
        return emailDuplicationValidator.checkExists(email);
    }

    @Override
    public Boolean checkNicknameDuplication(String nickname) {
        return nicknameDuplicationValidator.checkExists(nickname);
    }
}
