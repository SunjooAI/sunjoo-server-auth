package com.sunjoo.auth.domain.dto;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponseDto {
    long userNo;
    String id;
    String name;
    String type;

    public UserInfoResponseDto(long userNo, String name, String type) {
        this.userNo = userNo;
        this.name = name;
        this.type = type;
    }
}
