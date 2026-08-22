package com.mikey1201.managers;

/**
 * Thrown when a database operation fails.
 *
 * Unchecked so it can surface through APIs that cannot declare checked exceptions
 * (e.g. Vault's Economy methods), while still being catchable at command boundaries
 * so users get a clear error instead of silent data loss or success messages for
 * operations that never happened.
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}