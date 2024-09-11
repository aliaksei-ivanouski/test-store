package org.aivanouski.store.error;

public class IllegalIngredientsFoundException extends RuntimeException {
    public IllegalIngredientsFoundException(String message) {
        super(message);
    }
}
