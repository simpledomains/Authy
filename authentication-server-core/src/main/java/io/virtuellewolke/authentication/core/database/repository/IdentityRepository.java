package io.virtuellewolke.authentication.core.database.repository;

import io.virtuellewolke.authentication.core.database.entity.Identity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface IdentityRepository extends CrudRepository<Identity, Integer> {
    Optional<Identity> findByUsername(String username);

    Optional<Identity> findByUsernameOrEmail(String username, String email);

    Optional<Identity> findByApiToken(String token);

    List<Identity> findByAdmin(boolean isAdmin);

    List<Identity> findAll();

    List<Identity> findAllByAuthoritiesId(Integer id);
}