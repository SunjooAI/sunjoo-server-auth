package com.sunjoo.auth.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class NickNameResponseDto {
    long userNo;
    String id;
    String nickname; // 닉네임
    String type;

    public NickNameResponseDto(long userNo, String nickname, String type) {
        this.userNo = userNo;
        this.nickname = nickname;
        this.type = type;
    }
}
