package de.reynok.authentication.core.backend.database.repository;

import de.reynok.authentication.core.backend.database.entity.Identity;
import de.reynok.authentication.core.backend.database.entity.TPMCredential;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TPMCredentialRepository extends CrudRepository<TPMCredential, Integer> {
    public List<TPMCredential> findAllByIdentity(Identity identity);
}