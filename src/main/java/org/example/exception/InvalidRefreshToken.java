package org.example.exception;

public class InvalidRefreshToken extends RuntimeException {
    public InvalidRefreshToken(String message) {
        super(message);
    }
}
