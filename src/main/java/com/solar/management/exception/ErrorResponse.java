package com.solar.management.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int errorCode;
    private String errorMessage;
    private String message;

    public ErrorResponse(LocalDateTime timestamp, int errorCode, String errorMessage, String message) {
        this.timestamp = timestamp;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.message = message;
    }
}
