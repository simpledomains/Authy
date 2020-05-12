package io.virtuellewolke.authentication.core.database.repository;

import io.virtuellewolke.authentication.core.database.entity.ClientAuthCert;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import org.springframework.data.repository.CrudRepository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

public interface ClientAuthCertRepository extends CrudRepository<ClientAuthCert, BigInteger> {
    List<ClientAuthCert> findByIdentity(Identity identity);

    List<ClientAuthCert> findAll();

    List<ClientAuthCert> findAllByRevokedAtLessThanAndRevokedAtIsNotNull(LocalDateTime checkDate);

    void deleteAllByIdentityId(Integer identityId);
}