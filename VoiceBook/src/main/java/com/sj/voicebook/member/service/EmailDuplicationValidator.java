package com.sj.voicebook.member.service;

import com.sj.voicebook.global.exception.BusinessException;
import com.sj.voicebook.global.exception.ErrorCode;
import com.sj.voicebook.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Component
public class EmailDuplicationValidator {
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public boolean isEmailDuplicated(String email) {
        if(!StringUtils.hasText(email)){
            throw new BusinessException(ErrorCode.NOT_OR_NULL_FILE_REQUIRED);
        }
        return memberRepository.existsByEmail(email);
    }
}
