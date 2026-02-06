package com.taskflow.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }

    public UsernameAlreadyExistsException(String username, String message) {
        super(String.format("Username '%s' already exists. %s", username, message));
    }
}
