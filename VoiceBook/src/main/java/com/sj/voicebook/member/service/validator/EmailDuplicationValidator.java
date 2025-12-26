package com.sj.voicebook.member.service.validator;

import com.sj.voicebook.global.exception.BusinessException;
import com.sj.voicebook.global.exception.ErrorCode;
import com.sj.voicebook.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Component
public class EmailDuplicationValidator {
    private final MemberRepository memberRepository;

    /**
     * 회원가입용: 중복이면 예외를 던진다 (Command 성격)
     */

    public void validate(String email) {
        if (!StringUtils.hasText(email)) {
            throw new BusinessException(ErrorCode.NOT_OR_NULL_INPUT_REQUIRED);
        }
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATION);
        }
    }

    /**
     * 단순 확인용: 중복 여부를 반환한다 (Query 성격)
     */
    public boolean checkExists(String email) {
        if (!StringUtils.hasText(email)) {
            return false; // 혹은 예외 처리, 정책에 따라 다름
        }
        return memberRepository.existsByEmail(email);
    }
}
