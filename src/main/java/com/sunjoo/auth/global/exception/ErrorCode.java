package com.sunjoo.auth.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    DUPLICATED_USER_ID(HttpStatus.CONFLICT, "아이디가 중복됩니다."),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB 에러");

    private HttpStatus httpStatus;
    private String message;
}
