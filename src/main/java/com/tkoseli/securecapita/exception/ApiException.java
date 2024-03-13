package com.tkoseli.securecapita.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
