package de.reynok.authentication.core.logic.database.repository;

import de.reynok.authentication.core.api.models.Authority;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AuthorityRepository extends CrudRepository<Authority, Integer> {
    Optional<Authority> findByName(String name);
}