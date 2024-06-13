package com.sunjoo.auth.domain.service;

import com.sunjoo.auth.domain.User;
import com.sunjoo.auth.domain.UserRepository;
import com.sunjoo.auth.domain.dto.UserRegisterRequestDto;
import com.sunjoo.auth.domain.dto.UserRegisterResponseDto;
import com.sunjoo.auth.global.exception.AppException;
import com.sunjoo.auth.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Override
    public UserRegisterResponseDto register(UserRegisterRequestDto registerRequestDto) {
        // 아이디 중복 확인
        userJoinValid(registerRequestDto.getId());

        User saved = userRepository.save(
                User.builder()
                        .id(registerRequestDto.getId())
                        .password(passwordEncoder.encode(registerRequestDto.getPassword()))
                        .name(registerRequestDto.getName())
                        .type("DEFAULT")
                        .createdAt(LocalDate.now())
                        .build());
        return new UserRegisterResponseDto(saved);
    }

    private void userJoinValid(String id) {
        userRepository.findById(id)
                .ifPresent(user -> {throw new AppException(ErrorCode.DUPLICATED_USER_ID);});
    }
}
