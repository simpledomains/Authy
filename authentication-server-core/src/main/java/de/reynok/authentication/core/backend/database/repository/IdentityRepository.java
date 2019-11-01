package de.reynok.authentication.core.backend.database.repository;

import de.reynok.authentication.core.backend.database.entity.Identity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface IdentityRepository extends CrudRepository<Identity, Integer> {
    Optional<Identity> findByUsername(String username);
    Optional<Identity> findByApiToken(String token);
    List<Identity> findByAdmin(boolean isAdmin);
}