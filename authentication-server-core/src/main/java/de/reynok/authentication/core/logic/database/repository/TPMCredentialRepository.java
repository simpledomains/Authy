package de.reynok.authentication.core.logic.database.repository;

import de.reynok.authentication.core.api.models.Identity;
import de.reynok.authentication.core.api.models.TPMCredential;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TPMCredentialRepository extends CrudRepository<TPMCredential, Integer> {
    public List<TPMCredential> findAllByIdentity(Identity identity);
}