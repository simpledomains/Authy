package de.reynok.authentication.core.backend.database.repository;

import de.reynok.authentication.core.backend.database.entity.Authority;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AuthorityRepository extends CrudRepository<Authority, Integer> {
    Optional<Authority> findByName(String name);

    List<Authority> findAll();
}