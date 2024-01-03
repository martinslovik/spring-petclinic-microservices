package org.springframework.samples.petclinic.customers.request;

import jakarta.validation.constraints.Min;

public record FindOwnerByIdRequest(@Min(1) int id) {
}
