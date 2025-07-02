package com.vlad.app.exception;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {
    private final int statusCode;

    public BadRequestException(String message) {
        super(message);
        this.statusCode = 400;
    }
}
