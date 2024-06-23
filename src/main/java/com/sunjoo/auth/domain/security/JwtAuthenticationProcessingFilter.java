package com.sunjoo.auth.domain.security;

import com.sunjoo.auth.AuthApplication;
import com.sunjoo.auth.domain.User;
import com.sunjoo.auth.domain.UserRepository;
import com.sunjoo.auth.domain.service.JwtService;
import com.sunjoo.auth.domain.service.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final RedisService redisService;
    private final UserRepository userRepository;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    private final String NO_CHECK_URL = "/login";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(request.getRequestURI().equals(NO_CHECK_URL)) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = jwtService
                .extractRefreshToken(request)
                .filter(jwtService::isTokenValid)
                .orElse(null);

        if(refreshToken != null) {
            checkRefreshTokenAndReIssueAccessToken(response, refreshToken);
            return;
        }
        checkAcceessTokenAndAuthentication(request, response, filterChain);
    }

    private void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken) {
       Long no = jwtService.extractUserNo(refreshToken).orElse(null);

       if(no != null) {
           User foundUser = userRepository.findByUserNo(no).orElse(null);
           if(foundUser != null) {
               String storedRefreshToken = redisService.getValue(foundUser.getId());
               if(storedRefreshToken.equals(refreshToken)) {
                   userRepository.findByUserNo(no).ifPresent(
                           user-> {
                               String newAccessToken = jwtService.createAccessToken(user.getUserNo());
                               jwtService.sendAccessToken(response, newAccessToken);
                           }
                   );
               }
           }
       }
    }

    private void checkAcceessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        jwtService.extractAccessToken(request).filter(jwtService::isTokenValid).ifPresent(
                accessToken -> jwtService.extractUserNo(accessToken).ifPresent(
                        userNo -> userRepository.findByUserNo(userNo).ifPresent(
                                user -> saveAuthentication(user)
                        )
                )
        );
        filterChain.doFilter(request,response);
    }

    private void saveAuthentication(User user) {
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,authoritiesMapper.mapAuthorities(userDetails.getAuthorities()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();//5
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}
