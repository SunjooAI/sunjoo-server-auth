package com.sunjoo.auth.domain.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

public interface JwtService {
    String createAccessToken(long userNo);
    String createRefreshToken();

    void updateRefreshToken(String id, String refreshToken);
    void destroyRefreshToken(String id);

    void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken);
    void sendAccessToken(HttpServletResponse response, String accessToken);

    Optional<String> extractAccessToken(HttpServletRequest request);
    Optional<String> extractRefreshToken(HttpServletRequest request);
    Optional<String> extractId(String refreshToken);
    Optional<Long> extractUserNo(String accessToken);

    void setAccessTokenHeader(HttpServletResponse response, String accessToken);
    void setRefreshTokenHeader(HttpServletResponse response, String refreshToken);
    boolean isTokenValid(String token);
}
