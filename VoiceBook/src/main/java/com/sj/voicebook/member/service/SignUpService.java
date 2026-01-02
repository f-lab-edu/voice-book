package com.sj.voicebook.member.service;

import com.sj.voicebook.member.dto.api.SignUpRequest;

public interface SignUpService {
    Long signUp(SignUpRequest request);
    Boolean checkEmailDuplication(String email);
    Boolean checkNicknameDuplication(String nickname);


}
