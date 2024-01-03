package org.springframework.samples.petclinic.customers.event;

import jakarta.validation.constraints.NotNull;
import org.springframework.samples.petclinic.customers.model.Owner;

public record OwnerUpdatedEvent(@NotNull Owner owner) {
}
