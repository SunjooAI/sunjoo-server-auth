package com.sunjoo.auth.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.SQLException;

@Getter
@AllArgsConstructor
public class ErrorDto {
    private String errorCode;
    private String message;

    public ErrorDto(AppException e) {
        this.errorCode = e.getErrorCode().toString();
        this.message = e.getErrorCode().getMessage();
    }

    public ErrorDto(ErrorCode errorCode) {
        this.errorCode = errorCode.toString();
        this.message = errorCode.getMessage();
    }

    public ErrorDto(SQLException e) {
        this.errorCode = ErrorCode.DATABASE_ERROR.toString();
        this.message = ErrorCode.DATABASE_ERROR.getMessage();
    }
}
