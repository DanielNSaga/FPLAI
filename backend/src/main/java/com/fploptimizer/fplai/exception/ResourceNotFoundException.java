package com.fploptimizer.fplai.exception;

/**
 * Exception thrown when a resource is not found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}