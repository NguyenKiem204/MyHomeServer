package com.kiemnv.SpringSecurityJWT.exception;

public class PendingApprovalException extends RuntimeException {
    public PendingApprovalException(String message) {
        super(message);
    }
}
