package org.example.exception;

public class InvalidProductUpdated extends RuntimeException {
  public InvalidProductUpdated(String message) {
    super(message);
  }
}
