package org.springframework.samples.petclinic.customers.actor;

import akka.actor.AbstractLoggingActor;
import akka.japi.pf.ReceiveBuilder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.samples.petclinic.customers.event.OwnerCreatedEvent;
import org.springframework.samples.petclinic.customers.event.OwnerUpdatedEvent;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AnalyticsActor extends AbstractLoggingActor {

    @Override
    public Receive createReceive() {
        return ReceiveBuilder
            .create()
            .match(OwnerCreatedEvent.class, this::logOwnerCreatedEvent)
            .match(OwnerUpdatedEvent.class, this::logOwnerUpdatedEvent)
            .matchAny(msg -> {
                log().warning("AnalyticsActor: Unhandled message received: " + msg);
                unhandled(msg);
            })
            .build();
    }

    private void sleep() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void logOwnerCreatedEvent(OwnerCreatedEvent event) {
        sleep();
        log().info("AnalyticsActor: Received message: " + event.toString());
    }

    private void logOwnerUpdatedEvent(OwnerUpdatedEvent event) {
        sleep();
        log().info("AnalyticsActor: Received message: " + event.toString());
    }
}
