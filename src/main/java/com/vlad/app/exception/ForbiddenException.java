package com.vlad.app.exception;

import lombok.Getter;

@Getter
public class ForbiddenException extends RuntimeException {
    private final int statusCode;

    public ForbiddenException(String message) {
        super(message);
        this.statusCode = 403;
    }

}
