package com.nova.narrativa.domain.user.error;

public class S3CustomException extends RuntimeException {
    public S3CustomException(String message) {
        super(message);
    }
}