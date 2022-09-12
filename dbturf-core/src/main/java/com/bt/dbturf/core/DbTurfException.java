package com.bt.dbturf.core;

public class DbTurfException extends RuntimeException {

    public DbTurfException() {
    }

    public DbTurfException(String message) {
        super(message);
    }

    public DbTurfException(Throwable cause) {
        super(cause);
    }

    public DbTurfException(String message, Throwable cause) {
        super(message, cause);
    }

    public DbTurfException(String message, Throwable cause, boolean enableSuppression,
                           boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
