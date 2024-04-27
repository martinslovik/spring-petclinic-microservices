package org.springframework.samples.petclinic.customers.service;

import akka.pattern.CircuitBreakerOpenException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Optional;

import static org.springframework.samples.petclinic.customers.service.OwnerServiceImpl.RETRY_BACKOFF_DELAY;
import static org.springframework.samples.petclinic.customers.service.OwnerServiceImpl.RETRY_MAX_ATTEMPTS;

public interface OwnerService {

    @Transactional(rollbackFor = Exception.class)
    @Retryable(
        retryFor = RuntimeException.class,
        maxAttempts = RETRY_MAX_ATTEMPTS,
        backoff = @Backoff(delay = RETRY_BACKOFF_DELAY),
        noRetryFor = {UndeclaredThrowableException.class, CircuitBreakerOpenException.class}
    )
    Owner save(Owner owner) throws Exception;

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    @Retryable(
        retryFor = RuntimeException.class,
        maxAttempts = RETRY_MAX_ATTEMPTS,
        backoff = @Backoff(delay = RETRY_BACKOFF_DELAY),
        noRetryFor = {UndeclaredThrowableException.class, CircuitBreakerOpenException.class}
    )
    Optional<Owner> findById(int id) throws Exception;

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    @Retryable(
        retryFor = RuntimeException.class,
        maxAttempts = RETRY_MAX_ATTEMPTS,
        backoff = @Backoff(delay = RETRY_BACKOFF_DELAY),
        noRetryFor = {UndeclaredThrowableException.class, CircuitBreakerOpenException.class}
    )
    List<Owner> findAll() throws Exception;

    @Transactional(rollbackFor = Exception.class)
    @Retryable(
        retryFor = RuntimeException.class,
        maxAttempts = RETRY_MAX_ATTEMPTS,
        backoff = @Backoff(delay = RETRY_BACKOFF_DELAY),
        noRetryFor = {UndeclaredThrowableException.class, CircuitBreakerOpenException.class}
    )
    void update(int id, Owner owner) throws Exception;
}
