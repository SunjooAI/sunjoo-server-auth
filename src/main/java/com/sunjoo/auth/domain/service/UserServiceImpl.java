package com.sunjoo.auth.domain.service;

import com.nimbusds.jose.shaded.gson.JsonElement;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import com.sunjoo.auth.domain.User;
import com.sunjoo.auth.domain.UserRepository;
import com.sunjoo.auth.domain.dto.KakaoLoginRequestDto;
import com.sunjoo.auth.domain.dto.KakaoLoginResponseDto;
import com.sunjoo.auth.domain.dto.UserRegisterRequestDto;
import com.sunjoo.auth.domain.dto.UserRegisterResponseDto;
import com.sunjoo.auth.global.exception.AppException;
import com.sunjoo.auth.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;

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

    private void userJoinValid(String id) {
        userRepository.findById(id)
                .ifPresent(user -> {throw new AppException(ErrorCode.DUPLICATED_USER_ID);});
    }
}
