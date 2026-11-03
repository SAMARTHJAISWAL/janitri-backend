package com.janitri.backend.exceptions;

/** Thrown when a login attempt uses an unknown email or a wrong password. */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
