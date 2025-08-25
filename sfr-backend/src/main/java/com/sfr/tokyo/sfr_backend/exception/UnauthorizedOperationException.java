package com.sfr.tokyo.sfr_backend.exception;

public class UnauthorizedOperationException extends RuntimeException {
    public UnauthorizedOperationException(String msg) {
        super(msg);
    }
}
