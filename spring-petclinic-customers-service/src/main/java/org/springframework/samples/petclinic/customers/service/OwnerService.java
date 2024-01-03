package org.springframework.samples.petclinic.customers.service;

import org.springframework.samples.petclinic.customers.model.Owner;

import java.util.List;
import java.util.Optional;

public interface OwnerService {

    Owner save(Owner owner) throws Exception;

    Optional<Owner> findById(int id) throws Exception;

    List<Owner> findAll() throws Exception;

    void update(int id, Owner owner) throws Exception;
}
