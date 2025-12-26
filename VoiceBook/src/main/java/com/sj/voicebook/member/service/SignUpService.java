package com.sj.voicebook.member.service;

import com.sj.voicebook.member.dto.application.SignUpCommand;

public interface SignUpService {
    Long signUp(SignUpCommand command);
    Boolean checkEmailDuplication(String email);
    Boolean checkNicknameDuplication(String nickname);


}
