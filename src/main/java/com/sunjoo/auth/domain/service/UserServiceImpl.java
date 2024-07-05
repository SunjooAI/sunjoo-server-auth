package com.sunjoo.auth.domain.service;

import com.nimbusds.jose.shaded.gson.JsonElement;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import com.sunjoo.auth.domain.User;
import com.sunjoo.auth.domain.UserRepository;
import com.sunjoo.auth.domain.dto.*;
import com.sunjoo.auth.global.exception.AppException;
import com.sunjoo.auth.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

@RequiredArgsConstructor
@Service
@Slf4j
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



    // 카카오 로그인
    @Override
    public KakaoLoginRequestDto getKakaoUserInfo(String accessToken) {
        KakaoLoginRequestDto kakaoRequest = new KakaoLoginRequestDto();
        String postURL = "https://kapi.kakao.com/v2/user/me";

        try {
            URL url = new URL(postURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = conn.getResponseCode();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            StringBuilder result = new StringBuilder();

            while((line = br.readLine()) != null) {
                result.append(line);
            }

            JsonElement element = JsonParser.parseString(result.toString());
            JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
            JsonObject kakaoAccount = element.getAsJsonObject().get("kakao_account").getAsJsonObject();

            String name = properties.getAsJsonObject().get("nickname").getAsString();
            kakaoRequest.setName(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return kakaoRequest;
    }

    @Override
    public KakaoLoginResponseDto kakaoLogin(KakaoLoginRequestDto kakaoRequest) {
        KakaoLoginResponseDto kakaoResponse = new KakaoLoginResponseDto();

        userRepository.findByName(kakaoRequest.getName()).ifPresentOrElse(
                user -> {
                    kakaoResponse.setUserNo(user.getUserNo());
                    kakaoResponse.setName(user.getName());
                    kakaoResponse.setType("KAKAO");
                },
                () -> {
                    User newKakaoLogin = userRepository.save(
                            User.builder()
                                    .name(kakaoRequest.getName())
                                    .type("KAKAO")
                                    .createdAt(LocalDate.now())
                                    .build());
                    kakaoResponse.setUserNo(newKakaoLogin.getUserNo());
                    kakaoResponse.setType("KAKAO");
                    kakaoResponse.setName(newKakaoLogin.getName());
                }
                );

        return kakaoResponse;
    }

    @Override
    public UserInfoResponseDto getUserInfo(long userNo) {
        UserInfoResponseDto userInfoResponseDto = new UserInfoResponseDto();
        userRepository.findByUserNo(userNo).ifPresentOrElse(
            user->{
                if(user.getType().equals("DEFAULT")) userInfoResponseDto.setId(user.getId());
                userInfoResponseDto.setUserNo(user.getUserNo());
                userInfoResponseDto.setName(user.getName());
                userInfoResponseDto.setType(user.getType());
            }, () -> {throw new AppException(ErrorCode.USER_NOT_FOUND);}
        );
        return userInfoResponseDto;
    }

    @Transactional
    @Override
    public NickNameResponseDto updateNickName(long userNo, String nickName) {
        NickNameResponseDto nickNameResponseDto = new NickNameResponseDto();
        User user = userRepository.findByUserNo(userNo).orElseThrow(() -> {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        });

        user.setName(nickName);
        nickNameResponseDto.setNickname(user.getName());
        nickNameResponseDto.setUserNo(user.getUserNo());
        nickNameResponseDto.setType(user.getType());

        if(user.getType().equals("DEFAULT")) {
            nickNameResponseDto.setId(user.getId());
        }
        return nickNameResponseDto;
    }

    @Transactional
    @Override
    public void updateNewPassword(String id, String newPassword) {
        User user = userRepository.findById(id).orElseThrow(() -> {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        });

        user.setPassword(passwordEncoder.encode(newPassword));
    }

    @Override
    public GoogleLoginResponseDto googleLogin(GoogleLoginRequestDto googleRequest) {
        // 이메일로 가입되어 있는지 확인

        GoogleLoginResponseDto googleResponseDto = new GoogleLoginResponseDto();
        log.info("가입 이메일 : " + googleRequest.getEmail());
        userRepository.findByEmail(googleRequest.getEmail()).ifPresentOrElse(
                user -> {  // 만약에 가입된 이메일이라면 바로 로그인 진행
                    log.info(user.getEmail());
                    googleResponseDto.setUserNo(user.getUserNo());
                    googleResponseDto.setEmail(user.getEmail());
                    googleResponseDto.setName(user.getName());
                    googleResponseDto.setType(user.getType());
                }, () -> {  // 가입되어 있지 않으면 가입 후 로그인 진행
                    log.info("가입되지 않은 회원");
                    User newGoogleLogin = userRepository.save(
                            User.builder()
                                    .name(googleRequest.getName())
                                    .email(googleRequest.getEmail())
                                    .type("GOOGLE")
                                    .createdAt(LocalDate.now())
                                    .build());
                    googleResponseDto.setUserNo(newGoogleLogin.getUserNo());
                    googleResponseDto.setEmail(newGoogleLogin.getEmail());
                    googleResponseDto.setName(newGoogleLogin.getName());
                    googleResponseDto.setType(newGoogleLogin.getType());
                }
        );
        return googleResponseDto;
    }

    private void userJoinValid(String id) {
        userRepository.findById(id)
                .ifPresent(user -> {throw new AppException(ErrorCode.DUPLICATED_USER_ID);});
    }
}
