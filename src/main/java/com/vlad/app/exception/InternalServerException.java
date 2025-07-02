package com.vlad.app.exception;

import lombok.Getter;

@Getter
public class InternalServerException extends RuntimeException {
    private final int statusCode;
    public InternalServerException(String message) {
        super(message);
        this.statusCode = 500;
    }
}
