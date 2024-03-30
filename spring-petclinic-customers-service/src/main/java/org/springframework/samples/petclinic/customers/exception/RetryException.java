package org.springframework.samples.petclinic.customers.exception;

public class RetryException extends RuntimeException {

    public RetryException() {
        super();
    }

    public RetryException(String message) {
        super(message);
    }
}
