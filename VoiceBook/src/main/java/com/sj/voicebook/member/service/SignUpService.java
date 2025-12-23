package com.sj.voicebook.member.service;

import com.sj.voicebook.member.dto.application.CreateUserCommand;

public interface SignUpService {
    String signUp(CreateUserCommand command);
    Boolean checkEmailDuplication(String email);
    Boolean checkNicknameDuplication(String nickname);


}
