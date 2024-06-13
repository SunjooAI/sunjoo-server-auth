package com.sunjoo.auth.domain.controller;

import com.sunjoo.auth.domain.dto.UserRegisterRequestDto;
import com.sunjoo.auth.domain.dto.UserRegisterResponseDto;
import com.sunjoo.auth.domain.service.UserService;
import com.sunjoo.auth.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RequiredArgsConstructor
@Slf4j
@RestController
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity register(@Validated @RequestBody UserRegisterRequestDto registerRequest, BindingResult br) throws SQLException {
        log.info("회원 가입 요청 requestDto : {}", registerRequest);

        UserRegisterResponseDto registerResponse = userService.register(registerRequest);
        return ResponseEntity.ok(Response.success(registerResponse));
    }
}
