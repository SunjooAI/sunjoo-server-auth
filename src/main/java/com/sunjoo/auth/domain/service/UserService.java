package com.sunjoo.auth.domain.service;

import com.sunjoo.auth.domain.dto.KakaoLoginRequestDto;
import com.sunjoo.auth.domain.dto.KakaoLoginResponseDto;
import com.sunjoo.auth.domain.dto.UserRegisterRequestDto;
import com.sunjoo.auth.domain.dto.UserRegisterResponseDto;

import java.util.HashMap;

public interface UserService {
    public UserRegisterResponseDto register(UserRegisterRequestDto registerRequestDto);
    public KakaoLoginRequestDto getKakaoUserInfo(String accessToken);
    public KakaoLoginResponseDto kakaoLogin(KakaoLoginRequestDto kakaoRequest);
}
