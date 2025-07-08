package com.kiemnv.SpringSecurityJWT.exception;

public class UserAccountStatusException extends RuntimeException {
    public UserAccountStatusException(String message) {
        super(message);
    }
}
