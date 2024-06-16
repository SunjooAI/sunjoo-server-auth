package com.sunjoo.auth.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunjoo.auth.domain.UserRepository;
import com.sunjoo.auth.domain.security.*;
import com.sunjoo.auth.domain.service.JwtService;
import com.sunjoo.auth.domain.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

@Log4j2
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;
    private final RedisService redisService;
    private final UserRepository userRepository;

    //private final IpAddressMatcher hasIpAddress = new IpAddressMatcher("192.168.0.5"); // 도커 네트워크 생성 시 설정한 서브넷 입력

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterAfter(jsonIdPasswordAuthenticationFilter(), LogoutFilter.class)
                .authorizeHttpRequests((authorize)->authorize
                        .requestMatchers("/register", "/", "/login").permitAll()
//                        .requestMatchers("/**").access((authentication, context) -> new AuthorizationDecision(hasIpAddress.matches(context.getRequest())))
                        .anyRequest().authenticated())
//                .authorizeHttpRequests((authorize) -> authorize
//                        .anyRequest().permitAll())
                .logout((logout) -> logout
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
                .addFilterAfter(jsonIdPasswordAuthenticationFilter(), LogoutFilter.class)
                .addFilterBefore(jwtAuthenticationProcessingFilter(), JsonIdPasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() throws Exception {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        DaoAuthenticationProvider provider = daoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public LoginSuccessJWTProvideHandler loginSuccessJWTProvideHandler() {
        return new LoginSuccessJWTProvideHandler(jwtService, redisService);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler();
    }

    @Bean
    public JsonIdPasswordAuthenticationFilter jsonIdPasswordAuthenticationFilter() throws Exception {
        JsonIdPasswordAuthenticationFilter jsonIdPasswordLoginFilter = new JsonIdPasswordAuthenticationFilter(objectMapper);
        jsonIdPasswordLoginFilter.setAuthenticationManager(authenticationManager());
        jsonIdPasswordLoginFilter.setAuthenticationSuccessHandler(loginSuccessJWTProvideHandler());
        jsonIdPasswordLoginFilter.setAuthenticationFailureHandler(loginFailureHandler());
        return jsonIdPasswordLoginFilter;
    }

    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
        JwtAuthenticationProcessingFilter jsonUserIdPasswordLoginFilter = new JwtAuthenticationProcessingFilter(jwtService, redisService, userRepository);
        return jsonUserIdPasswordLoginFilter;
    }
}
