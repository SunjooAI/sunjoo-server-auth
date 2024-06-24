package com.sunjoo.auth.domain.controller;

import com.sunjoo.auth.domain.dto.*;
import com.sunjoo.auth.domain.security.UserDetailsImpl;
import com.sunjoo.auth.domain.service.JwtService;
import com.sunjoo.auth.domain.service.RedisService;
import com.sunjoo.auth.domain.service.UserService;
import com.sunjoo.auth.domain.service.SendMailService;
import com.sunjoo.auth.global.Response;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RequiredArgsConstructor
@Slf4j
@RestController
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;
    private final RedisService redisService;
    private final SendMailService sendMailService;

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

    @GetMapping("/userinfo")
    public ResponseEntity getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("정보 조회 요청 회원 번호 : " + userDetails.getUserNo());
        UserInfoResponseDto userInfoResponseDto = userService.getUserInfo(userDetails.getUserNo());
        return ResponseEntity.ok(Response.success(userInfoResponseDto));
    }

    @PutMapping("/nickname")
    public ResponseEntity updateUserNickname(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody NickNameRequestDto requestDto) {
        NickNameResponseDto nickNameResponseDto = userService.updateNickName(userDetails.getUserNo(), requestDto.getNewNickName());
        return ResponseEntity.ok(Response.success(nickNameResponseDto));
    }

    @PostMapping("/reset-password/send-email")
    public ResponseEntity resetPwdGetEmail(@RequestBody ResetPwdEmailRequestDto requestDto) {
        String uuid = sendMailService.sendResetPwdEmail(requestDto.getEmail());
        return ResponseEntity.ok(Response.success(uuid));
    }

    @PutMapping("/new-password")
    public ResponseEntity newPwd(@RequestBody NewPwdRequestDto requestDto) {
        userService.updateNewPassword(requestDto.getId(), requestDto.getNewPassword());
        return ResponseEntity.ok("success update password");
    }
};