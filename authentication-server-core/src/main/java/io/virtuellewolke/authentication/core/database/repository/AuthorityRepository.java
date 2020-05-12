package io.virtuellewolke.authentication.core.database.repository;

import io.virtuellewolke.authentication.core.database.entity.Authority;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AuthorityRepository extends CrudRepository<Authority, Integer> {
    Optional<Authority> findByName(String name);

    List<Authority> findAll();
}