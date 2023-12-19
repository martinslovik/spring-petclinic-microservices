package org.springframework.samples.petclinic.customers.request;

import akka.actor.ActorRef;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import org.springframework.samples.petclinic.customers.model.Pet;

import java.util.List;

public record AddOwnerRequest(Integer id,
                              @NotBlank String firstName,
                              @NotBlank String lastName,
                              @NotBlank String address,
                              @NotBlank String city,
                              @NotBlank @Digits(fraction = 0, integer = 12) String telephone,
                              List<Pet> pets) {
}
