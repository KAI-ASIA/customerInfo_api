package com.kaiasia.app.service.customer.exception;

public class CustomException extends RuntimeException {
    public CustomException(String message, Throwable cause) {
        super(cause);
    }

    public CustomException(Throwable cause) {
        super(cause);
    }
}
