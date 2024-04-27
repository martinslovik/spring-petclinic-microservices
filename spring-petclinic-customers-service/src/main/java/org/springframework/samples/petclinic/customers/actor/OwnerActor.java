package org.springframework.samples.petclinic.customers.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.samples.petclinic.customers.actor.constants.CircuitBreakerConstants;
import org.springframework.samples.petclinic.customers.actor.supervisor.ExceptionSupervisorStrategy;
import org.springframework.samples.petclinic.customers.event.OwnerCreatedEvent;
import org.springframework.samples.petclinic.customers.event.OwnerUpdatedEvent;
import org.springframework.samples.petclinic.customers.integration.akka.SpringAkkaExtension;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.request.AddOwnerRequest;
import org.springframework.samples.petclinic.customers.request.FindAllOwnersRequest;
import org.springframework.samples.petclinic.customers.request.FindOwnerByIdRequest;
import org.springframework.samples.petclinic.customers.request.UpdateOwnerRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Actor responsible for handling owners.
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OwnerActor extends AbstractLoggingActor {

    private final CircuitBreaker circuitBreaker;
    private final OwnerRepository ownerRepository;
    private final ActorSystem actorSystem;
    private final SpringAkkaExtension springAkkaExtension;
    private final ActorRef analyticsActor;

    @Autowired
    public OwnerActor(OwnerRepository ownerRepository, ActorSystem actorSystem, SpringAkkaExtension springAkkaExtension) {
        this.ownerRepository = ownerRepository;
        this.actorSystem = actorSystem;
        this.springAkkaExtension = springAkkaExtension;
        this.analyticsActor = initAnalyticsActor();
        this.circuitBreaker = CircuitBreaker.create(
                getContext().getSystem().scheduler(),
                CircuitBreakerConstants.MAX_FAILURES,
                CircuitBreakerConstants.CALL_TIMEOUT,
                CircuitBreakerConstants.RESET_TIMEOUT)
            .addOnOpenListener(this::notifyOnOpen)
            .addOnHalfOpenListener(this::notifyOnHalfOpen)
            .addOnCloseListener(this::notifyOnClose);
    }

    private ActorRef initAnalyticsActor() {
        return actorSystem
            .actorOf(springAkkaExtension
                .props(SpringAkkaExtension.classNameToSpringName(AnalyticsActor.class)));
    }

    public static Props props() {
        return Props.create(OwnerActor.class, ExceptionSupervisorStrategy.strategy);
    }

    @Override
    public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
        log().error(reason, "OwnerActor restarting due to exception: {}", reason.getMessage());
        super.preRestart(reason, message);
    }

    @Override
    public void postRestart(Throwable reason) throws Exception {
        log().info("OwnerActor restarted");
        super.postRestart(reason);
    }

    @Override
    public void preStart() throws Exception {
        log().info("OwnerActor started");
        super.preStart();
    }

    @Override
    public void postStop() throws Exception {
        log().info("OwnerActor stopped");
        super.postStop();
    }

    public void notifyOnOpen() {
        log().warning("My CircuitBreaker is now open, and will not close for " + CircuitBreakerConstants.RESET_TIMEOUT.toSeconds() + " seconds");
    }

    public void notifyOnHalfOpen() {
        log().warning("My CircuitBreaker is now half open");
    }

    public void notifyOnClose() {
        log().warning("My CircuitBreaker is now closed");
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder
            .create()
            .match(AddOwnerRequest.class, this::save)
            .match(FindOwnerByIdRequest.class, this::findById)
            .match(FindAllOwnersRequest.class, request -> findAll())
            .match(UpdateOwnerRequest.class, this::update)
            .matchAny(msg -> {
                log().warning("OwnerActor: Unhandled message received: " + msg);
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
                analyticsActor.tell(new OwnerCreatedEvent(savedOwner), self());
                return self();
            });
        } catch (Exception e) {
            log().error("AddOwnerRequest: Exception occurred: " + e.getMessage());
            sender().tell(e, self());
            throw e;
        }
    }

    private void findById(FindOwnerByIdRequest findOwnerByIdRequest) {
        log().info("FindOwnerByIdRequest: Received message: " + findOwnerByIdRequest.id());
        try {
            this.circuitBreaker.callWithSyncCircuitBreaker(() -> {
                Owner owner = ownerRepository.findById(findOwnerByIdRequest.id()).orElseThrow();
                sender().tell(owner, self());
                return self();
            });
        } catch (Exception e) {
            log().error("FindOwnerByIdRequest: Exception occurred: " + e.getMessage());
            sender().tell(e, self());
            throw e;
        }
    }

    private void findAll() {
        log().info("FindAllOwnersRequest: Received message");
        try {
            this.circuitBreaker.callWithSyncCircuitBreaker(() -> {
                sender().tell(ownerRepository.findAll(), self());
                return self();
            });
        } catch (Exception e) {
            log().error("FindAllOwnersRequest: Exception occurred: " + e.getMessage());
            sender().tell(e, self());
            throw e;
        }
    }

    private void update(UpdateOwnerRequest updateOwnerRequest) {
        log().info("UpdateOwnerRequest: Received message: " + updateOwnerRequest.toString());
        try {
            this.circuitBreaker.callWithSyncCircuitBreaker(() -> {
                Owner owner = ownerRepository.findById(updateOwnerRequest.id()).orElseThrow();
                owner.setFirstName(updateOwnerRequest.firstName());
                owner.setLastName(updateOwnerRequest.lastName());
                owner.setAddress(updateOwnerRequest.address());
                owner.setCity(updateOwnerRequest.city());
                owner.setTelephone(updateOwnerRequest.telephone());
                Owner updatedOwner = ownerRepository.save(owner);
                sender().tell(updatedOwner, self());
                analyticsActor.tell(new OwnerUpdatedEvent(updatedOwner), self());
                return self();
            });
        } catch (Exception e) {
            log().error("UpdateOwnerRequest: Exception occurred: " + e.getMessage());
            sender().tell(e, self());
            throw e;
        }
    }
}
