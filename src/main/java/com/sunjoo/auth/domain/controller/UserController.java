package com.sunjoo.auth.domain.controller;

import com.sunjoo.auth.domain.dto.KakaoLoginRequestDto;
import com.sunjoo.auth.domain.dto.KakaoLoginResponseDto;
import com.sunjoo.auth.domain.dto.UserRegisterRequestDto;
import com.sunjoo.auth.domain.dto.UserRegisterResponseDto;
import com.sunjoo.auth.domain.service.JwtService;
import com.sunjoo.auth.domain.service.RedisService;
import com.sunjoo.auth.domain.service.UserService;
import com.sunjoo.auth.global.Response;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.HashMap;

@RequiredArgsConstructor
@Slf4j
@RestController
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;
    private final RedisService redisService;

    @PostMapping("/register")
    public ResponseEntity register(@Validated @RequestBody UserRegisterRequestDto registerRequest, BindingResult br) throws SQLException {
        log.info("회원 가입 요청 requestDto : {}", registerRequest);

        UserRegisterResponseDto registerResponse = userService.register(registerRequest);
        return ResponseEntity.ok(Response.success(registerResponse));
    }

    @GetMapping("/login/kakao")
    public ResponseEntity kakaoLogin(@RequestParam(required = false) String token, HttpServletResponse response) {
        try {
            KakaoLoginRequestDto kakaoRequest = userService.getKakaoUserInfo(token);

            // 카카오 로그인
            KakaoLoginResponseDto kakaoResponse = userService.kakaoLogin(kakaoRequest);
            String accessToken = jwtService.createAccessToken(kakaoResponse.getUserNo());
            String refreshToken = jwtService.createRefreshToken();

            jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
            redisService.setValues(kakaoResponse.getName(), refreshToken);

            // 우리 서버 jwt 넘겨주기
            response.setHeader("Authorization", "Bearer " + accessToken);
            return ResponseEntity.ok(Response.success(kakaoResponse));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}