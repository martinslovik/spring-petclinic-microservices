package org.springframework.samples.petclinic.customers.advice;

import akka.pattern.CircuitBreakerOpenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice
public class OwnerResourceExceptionHandler {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<String> handleHttpMessageNotReadableException(SQLIntegrityConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad Request: " + ex.getMessage());
    }

    @ExceptionHandler(CircuitBreakerOpenException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    ResponseEntity<String> handleHttpMessageNotReadableException(CircuitBreakerOpenException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Circuit breaker open: " + ex.getMessage());
    }
}
