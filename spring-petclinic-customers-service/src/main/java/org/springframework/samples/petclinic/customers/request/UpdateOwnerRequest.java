package org.springframework.samples.petclinic.customers.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateOwnerRequest(@Min(1) int id,
                                 @NotBlank String firstName,
                                 @NotBlank String lastName,
                                 @NotBlank String address,
                                 @NotBlank String city,
                                 @NotBlank @Digits(fraction = 0, integer = 12) String telephone) {
}
