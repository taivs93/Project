package org.example.exception;

public class InvalidDraftPackageException extends RuntimeException {
    public InvalidDraftPackageException(String message) {
        super(message);
    }
}
