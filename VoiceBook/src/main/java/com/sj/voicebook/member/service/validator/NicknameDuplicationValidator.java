package com.sj.voicebook.member.service.validator;

import com.sj.voicebook.global.exception.BusinessException;
import com.sj.voicebook.global.exception.ErrorCode;
import com.sj.voicebook.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class NicknameDuplicationValidator {
    private final MemberRepository memberRepository;

    public void validate(String nickname) {
        if (!StringUtils.hasText(nickname)) {
            throw new BusinessException(ErrorCode.NOT_OR_NULL_INPUT_REQUIRED);
        }
        if (memberRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_DUPLICATION);
        }
    }

    public boolean checkExists(String nickname) {
        if (!StringUtils.hasText(nickname)) {
            throw new BusinessException(ErrorCode.NOT_OR_NULL_INPUT_REQUIRED);
        }
        return memberRepository.existsByNickname(nickname);
    }
}
