package com.sunjoo.auth.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class UserRegisterRequestDto {
    private String id;
    private String password;
    private String name;
}
