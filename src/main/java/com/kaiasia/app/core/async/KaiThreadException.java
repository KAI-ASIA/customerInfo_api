package com.kaiasia.app.core.async;

// Lớp exception cho thread
public class KaiThreadException extends Exception{
    public KaiThreadException(String location, Throwable throwable) {
        super(location, throwable);
    }
}
