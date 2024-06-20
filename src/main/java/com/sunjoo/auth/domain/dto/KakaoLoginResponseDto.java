package com.sunjoo.auth.domain.dto;

import com.sunjoo.auth.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KakaoLoginResponseDto {
    private long userNo;
    private String name;
    private String type;

    public KakaoLoginResponseDto(User user) {
        this.userNo = user.getUserNo();
        this.name = user.getName();
        this.type = user.getType();
    }
}
