package com.sunjoo.auth.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class AppException extends RuntimeException{
    @NonNull
    private ErrorCode errorCode;
    private String message;
}
