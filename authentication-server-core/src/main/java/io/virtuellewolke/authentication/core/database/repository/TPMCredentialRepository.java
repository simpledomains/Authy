package io.virtuellewolke.authentication.core.database.repository;

import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.database.entity.TPMCredential;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TPMCredentialRepository extends CrudRepository<TPMCredential, Integer> {
    List<TPMCredential> findAllByIdentity(Identity identity);
}