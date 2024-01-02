package org.springframework.samples.petclinic.customers.actor.supervisor;

import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import akka.pattern.CircuitBreakerOpenException;
import scala.concurrent.duration.Duration;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.concurrent.TimeUnit;

public class ExceptionSupervisorStrategy {

    private ExceptionSupervisorStrategy() {
    }

    public static final SupervisorStrategy strategy =
        new OneForOneStrategy(
            5,
            Duration.create(10, TimeUnit.SECONDS),
            true,
            DeciderBuilder
                .match(ArithmeticException.class, e -> SupervisorStrategy.resume())
                .match(NullPointerException.class, e -> SupervisorStrategy.restart())
                .match(IllegalArgumentException.class, e -> SupervisorStrategy.stop())
                .match(SQLIntegrityConstraintViolationException.class, e -> SupervisorStrategy.restart())
                .match(CircuitBreakerOpenException.class, e -> SupervisorStrategy.resume())
                .matchAny(o -> SupervisorStrategy.escalate())
                .build());
}
