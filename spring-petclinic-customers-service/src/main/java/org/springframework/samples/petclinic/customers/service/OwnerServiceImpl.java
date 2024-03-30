package org.springframework.samples.petclinic.customers.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import lombok.extern.slf4j.Slf4j;
import org.springframework.samples.petclinic.customers.actor.OwnerActor;
import org.springframework.samples.petclinic.customers.actor.constants.CircuitBreakerConstants;
import org.springframework.samples.petclinic.customers.exception.RetryException;
import org.springframework.samples.petclinic.customers.integration.akka.SpringAkkaExtension;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.request.AddOwnerRequest;
import org.springframework.samples.petclinic.customers.request.FindAllOwnersRequest;
import org.springframework.samples.petclinic.customers.request.FindOwnerByIdRequest;
import org.springframework.samples.petclinic.customers.request.UpdateOwnerRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Service for handling owners.
 * Using @Component annotation to not include as chaos monkey watcher target.
 */
@Slf4j
@Component
public class OwnerServiceImpl implements OwnerService {

    private static final int TIMEOUT_DURATION = 60;
    private static final int MAX_RETRIES = CircuitBreakerConstants.MAX_FAILURES + 1;
    private int retryCount = 0;

    private final Timeout timeout;
    private final ActorSystem actorSystem;
    private final SpringAkkaExtension springAkkaExtension;
    private final ActorRef ownerActor;

    public OwnerServiceImpl(ActorSystem actorSystem, SpringAkkaExtension springAkkaExtension) {
        this.actorSystem = actorSystem;
        this.springAkkaExtension = springAkkaExtension;
        this.timeout = new Timeout(Duration.create(TIMEOUT_DURATION, TimeUnit.SECONDS));
        this.ownerActor = initOwnerActor();
    }

    private ActorRef initOwnerActor() {
        return actorSystem
            .actorOf(springAkkaExtension
                .props(SpringAkkaExtension.classNameToSpringName(OwnerActor.class)));
    }

    private static <T> T await(Object awaitableResult, Class<T> type) throws Exception {
        if (type.isInstance(awaitableResult)) {
            return type.cast(awaitableResult);
        } else if (awaitableResult instanceof Exception) {
            throw (Exception) awaitableResult;
        } else {
            throw new IllegalStateException("[Await][Unknown] Unknown result type");
        }
    }

    @Override
    @Transactional
    public Owner save(Owner owner) {
        Owner savedOwner = null;
        while (retryCount < MAX_RETRIES) {
            try {
                Future<Object> future = Patterns.ask(ownerActor, new AddOwnerRequest(
                        owner.getId(),
                        owner.getFirstName(),
                        owner.getLastName(),
                        owner.getAddress(),
                        owner.getCity(),
                        owner.getTelephone(),
                        owner.getPets()),
                    timeout);

                Object result = Await.result(future, timeout.duration());
                savedOwner = await(result, Owner.class);
                break;
            } catch (Exception e) {
                retryCount++;
                log.info("Failed to save owner, retrying...");
            }
        }

        if (savedOwner == null) {
            throw new RetryException("Failed to save owner after multiple retries");
        }
        return savedOwner;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Owner> findById(int id) {
        Optional<Owner> owner = Optional.empty();
        while (retryCount < MAX_RETRIES) {
            try {
                Future<Object> future = Patterns.ask(ownerActor, new FindOwnerByIdRequest(id), timeout);
                Object result = Await.result(future, timeout.duration());
                owner = Optional.ofNullable(await(result, Owner.class));
                break;
            } catch (Exception e) {
                retryCount++;
                log.info("Failed to fetch owner, retrying...");
            }
        }

        if (owner == null || owner.isEmpty()) {
            throw new RetryException("Failed to fetch owner after multiple retries");
        }
        return owner;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Owner> findAll() {
        List<Owner> owners = null;

        while (retryCount < MAX_RETRIES) {
            try {
                Future<Object> future = Patterns.ask(ownerActor, new FindAllOwnersRequest(), timeout);
                Object result = Await.result(future, timeout.duration());
                owners = await(result, List.class);
                break;
            } catch (Exception e) {
                retryCount++;
                log.info("Exception: " + e.getMessage());
                log.info("Failed to fetch owners, retrying...");
            }
        }

        if (owners == null) {
            throw new RetryException("Failed to fetch owners after multiple retries");
        }

        return owners;
    }

    @Override
    @Transactional
    public void update(int id, Owner owner) {
        while (retryCount < MAX_RETRIES) {
            try {
                Future<Object> future = Patterns.ask(ownerActor, new UpdateOwnerRequest(
                        id,
                        owner.getFirstName(),
                        owner.getLastName(),
                        owner.getAddress(),
                        owner.getCity(),
                        owner.getTelephone()),
                    timeout);

                Object result = Await.result(future, timeout.duration());
                await(result, Owner.class);
                break;
            } catch (Exception e) {
                retryCount++;
                log.info("Failed to update owner, retrying...");
            }
        }
    }
}
