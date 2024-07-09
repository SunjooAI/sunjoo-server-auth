package com.sunjoo.auth.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class GoogleLoginResponseDto {
    private long userNo;
    private String name;
    private String type;
    private String email;
}
