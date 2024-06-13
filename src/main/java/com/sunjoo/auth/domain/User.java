package com.sunjoo.auth.domain;

import com.sunjoo.auth.domain.dto.UserRegisterRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@Table(name="user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_no")
    private Long userNo;

    private String email; // 소셜 로그인 아이디 저장

    private String id; // 자체 회원가입 시 사용

    private String password; // 자체 회원가입 시 사용

    private String name;

    private String type;

    @Column(name = "created_at")
    private LocalDate createdAt;
}
