package org.aivanouski.store.error;

public class IllegalStatusException extends RuntimeException {
    public IllegalStatusException(String message) {
        super(message);
    }
}
