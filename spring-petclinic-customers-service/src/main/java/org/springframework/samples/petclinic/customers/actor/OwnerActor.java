package org.springframework.samples.petclinic.customers.actor;

import akka.actor.AbstractLoggingActor;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.samples.petclinic.customers.actor.constants.CircuitBreakerConstants;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.request.AddOwnerRequest;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OwnerActor extends AbstractLoggingActor {

    private final CircuitBreaker circuitBreaker;
    private final OwnerRepository ownerRepository;

    @Autowired
    public OwnerActor(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
        this.circuitBreaker = CircuitBreaker.create(
                getContext().getSystem().scheduler(),
                CircuitBreakerConstants.MAX_FAILURES,
                CircuitBreakerConstants.CALL_TIMEOUT,
                CircuitBreakerConstants.RESET_TIMEOUT)
            .addOnOpenListener(this::notifyMeOnOpen)
            .addOnHalfOpenListener(this::notifyMeOnHalfOpen)
            .addOnCloseListener(this::notifyMeOnClose);
    }

    public void notifyMeOnOpen() {
        log().warning("My CircuitBreaker is now open, and will not close for " + CircuitBreakerConstants.RESET_TIMEOUT.toSeconds() + " seconds");
    }

    public void notifyMeOnHalfOpen() {
        log().warning("My CircuitBreaker is now half open");
    }

    public void notifyMeOnClose() {
        log().warning("My CircuitBreaker is now closed");
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder
            .create()
            .match(AddOwnerRequest.class, this::save)
            .matchAny(msg -> {
                log().warning("GreetingResultLoggerActor: Unhandled message received: " + msg);
                unhandled(msg);
            })
            .build();
    }

    private void save(AddOwnerRequest request) {
        log().info("AddOwnerRequest: Received message: " + request.toString());
        try {
            this.circuitBreaker.callWithSyncCircuitBreaker(() -> {
                Owner ownerToSave = new Owner();
                ownerToSave.setFirstName(request.firstName());
                ownerToSave.setLastName(request.lastName());
                ownerToSave.setAddress(request.address());
                ownerToSave.setCity(request.city());
                ownerToSave.setTelephone(request.telephone());
                request.pets().forEach(ownerToSave::addPet);
                log().info("AddOwnerRequest: Saving owner: " + ownerToSave);
                Owner savedOwner = ownerRepository.save(ownerToSave);
                sender().tell(savedOwner, self());
                return self();
            });
        } catch (Exception e) {
            log().error("AddOwnerRequest: Exception occurred: " + e);
            log().error("AddOwnerRequest: Exception occurred: " + e.getMessage());
            sender().tell(e, self());
        }
    }
}
