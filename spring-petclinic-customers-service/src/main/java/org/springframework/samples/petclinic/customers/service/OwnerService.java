package org.springframework.samples.petclinic.customers.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import org.springframework.samples.petclinic.customers.actor.OwnerActor;
import org.springframework.samples.petclinic.customers.integration.akka.SpringAkkaExtension;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.request.AddOwnerRequest;
import org.springframework.stereotype.Service;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

@Service
public class OwnerService {

    private final ActorSystem actorSystem;
    private final SpringAkkaExtension springAkkaExtension;
    private final Timeout timeout;
    private final ActorRef ownerActor;

    public OwnerService(ActorSystem actorSystem, SpringAkkaExtension springAkkaExtension) {
        this.actorSystem = actorSystem;
        this.springAkkaExtension = springAkkaExtension;
        this.timeout = new Timeout(Duration.create(10, TimeUnit.SECONDS));
        this.ownerActor = getOwnerActor();
    }

    public Owner save(Owner owner) throws Exception {
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
        return await(result, Owner.class);
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

    private ActorRef getOwnerActor() {
        return actorSystem
            .actorOf(springAkkaExtension
                .props(SpringAkkaExtension.classNameToSpringName(OwnerActor.class)));
    }
}
