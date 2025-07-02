package com.vlad.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handle400Exception(BadRequestException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.valueOf(ex.getStatusCode()));
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorResponse> handle500Exception(InternalServerException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.valueOf(ex.getStatusCode()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handle403Exception(ForbiddenException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.valueOf(ex.getStatusCode()));
    }

    public record ErrorResponse(String error) {
    }
}
