package com.kaiasia.app.service.customer.exception;

public class InsertFailedException extends CustomException {
    public InsertFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsertFailedException(Throwable cause) {
        super(cause);
    }
}
