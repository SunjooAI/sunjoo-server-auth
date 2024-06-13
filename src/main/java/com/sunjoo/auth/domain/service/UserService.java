package com.sunjoo.auth.domain.service;

import com.sunjoo.auth.domain.dto.UserRegisterRequestDto;
import com.sunjoo.auth.domain.dto.UserRegisterResponseDto;

public interface UserService {
    public UserRegisterResponseDto register(UserRegisterRequestDto registerRequestDto);
}
