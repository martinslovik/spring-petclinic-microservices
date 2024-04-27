package org.springframework.samples.petclinic.customers.actor.constants;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class CircuitBreakerConstants {

    private CircuitBreakerConstants() {
    }

    public static final int MAX_FAILURES = 5;
    public static final FiniteDuration CALL_TIMEOUT = Duration.create(10, TimeUnit.SECONDS);
    public static final FiniteDuration RESET_TIMEOUT = Duration.create(10, TimeUnit.SECONDS);
}
