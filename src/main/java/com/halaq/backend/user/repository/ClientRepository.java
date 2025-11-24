package com.halaq.backend.user.repository;

import com.halaq.backend.user.entity.Client;
import com.halaq.backend.core.repository.AbstractRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends AbstractRepository<Client, Long> {

    /**
     * Finds a client by their email address.
     * @param email The email to search for.
     * @return An Optional containing the client if found.
     */
    Optional<Client> findByEmail(String email);
}