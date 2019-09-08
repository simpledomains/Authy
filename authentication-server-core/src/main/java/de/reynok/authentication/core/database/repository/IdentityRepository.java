package de.reynok.authentication.core.database.repository;

import de.reynok.authentication.core.database.entity.Identity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface IdentityRepository extends CrudRepository<Identity, Integer> {
    Optional<Identity> findByUsername(String username);
}