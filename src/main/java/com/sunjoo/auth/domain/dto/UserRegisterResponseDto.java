package com.sunjoo.auth.domain.dto;

import com.sunjoo.auth.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterResponseDto {
    private String id;
    private String name;
    private String type;

    public UserRegisterResponseDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.type = user.getType();
    }
}
