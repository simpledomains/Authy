package de.reynok.authentication.core.backend.database.repository;

import de.reynok.authentication.core.backend.database.entity.ClientAuthCert;
import de.reynok.authentication.core.backend.database.entity.Identity;
import org.springframework.data.repository.CrudRepository;

import java.math.BigInteger;
import java.util.List;

public interface ClientAuthCertRepository extends CrudRepository<ClientAuthCert, BigInteger> {
    List<ClientAuthCert> findByIdentity(Identity identity);

    List<ClientAuthCert> findAll();
}