package com.sunjoo.auth.domain.security;

import com.sunjoo.auth.domain.User;
import com.sunjoo.auth.domain.UserRepository;
import com.sunjoo.auth.domain.service.JwtService;
import com.sunjoo.auth.domain.service.RedisService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessJWTProvideHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final RedisService redisService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String id = extractId(authentication);
        userRepository.findById(id).ifPresent(
                user -> {
                    long userNo = user.getUserNo();
                    String accessToken = jwtService.createAccessToken(userNo);
                    String refreshToken = jwtService.createRefreshToken();

                    jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
                    redisService.setValues(id, refreshToken);

                    log.info("로그인에 성공합니다 : id : {}", id);
                    log.info("AccessToken을 발급합니다. AccessToken : {}", accessToken);

                    try {
                        response.setHeader("Authorization", "Bearer " + accessToken);
                        response.getWriter().write("success");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private String extractId(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}
