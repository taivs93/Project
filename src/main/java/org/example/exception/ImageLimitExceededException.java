package org.example.exception;

public class ImageLimitExceededException extends RuntimeException {
    public ImageLimitExceededException(String message) {
        super(message);
    }
}
