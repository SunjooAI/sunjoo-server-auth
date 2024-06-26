package com.sunjoo.auth.domain.service;

import com.sunjoo.auth.domain.dto.*;
import jakarta.transaction.Transactional;

import java.util.HashMap;

public interface UserService {
    public UserRegisterResponseDto register(UserRegisterRequestDto registerRequestDto);
    public KakaoLoginRequestDto getKakaoUserInfo(String accessToken);
    public KakaoLoginResponseDto kakaoLogin(KakaoLoginRequestDto kakaoRequest);
    public UserInfoResponseDto getUserInfo(long userNo);
    public NickNameResponseDto updateNickName(long userNo, String nickName);
    public void updateNewPassword(String id, String newPassword);
}
