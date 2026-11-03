package com.janitri.backend.exceptions;

/** Thrown when creating something that must be unique (e.g. a user email) and already exists. */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
