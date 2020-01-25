package de.reynok.authentication.core;

import de.reynok.authentication.core.backend.database.entity.Authority;
import de.reynok.authentication.core.backend.database.entity.ClientAuthCert;
import de.reynok.authentication.core.backend.database.entity.Identity;
import de.reynok.authentication.core.backend.database.entity.Service;
import de.reynok.authentication.core.backend.database.repository.AuthorityRepository;
import de.reynok.authentication.core.backend.database.repository.ClientAuthCertRepository;
import de.reynok.authentication.core.backend.database.repository.IdentityRepository;
import de.reynok.authentication.core.backend.database.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AuthenticationServerInit {

    private final IdentityRepository       identityRepository;
    private final AuthorityRepository      authorityRepository;
    private final ServiceRepository        serviceRepository;
    private final ClientAuthCertRepository clientAuthCertRepository;

    @PostConstruct
    private void initialize() {

        if (serviceRepository.findAll().size() == 0) {
            Service applicationService = new Service();
            applicationService.setName("Authentication Service");
            applicationService.setMode(Service.ServiceMode.PUBLIC);
            applicationService.getAllowedUrls().add("/");
            applicationService.setEnabled(true);
            serviceRepository.save(applicationService);

            Service service = new Service();
            service.setName("Allow all");
            service.setEnabled(true);
            service.getAllowedUrls().add("*");
            service.setMode(Service.ServiceMode.PUBLIC);
            serviceRepository.save(service);
        }


        if (authorityRepository.findByName("admin").isEmpty()) {
            Authority adminRole = new Authority("admin");
            authorityRepository.save(adminRole);
        }

        if (identityRepository.findByAdmin(true).size() == 0) {
            Identity identity = new Identity();
            identity.setUsername("admin");
            identity.setPassword("admin");
            identity.setAdmin(true);
            identity.setEmail("admin@example.com");
            identity.setDisplayName("Administrator");
            identity.setApiToken("7KVEfk8dG7sxTGngUwpFAgyc89SHRR6jssCaeukDHUVSQbJx7exMR5GVBqdbGyyu");
            //identity.setOtpSecret("JBSWY3DPEHPK3PXP");

            identity.getAuthorities().add(authorityRepository.findByName("admin").get());

            identityRepository.save(identity);

            log.info("Adding Administrator Account (admin/admin) cuz there is no admin ?!?.");
        }

        if (log.isDebugEnabled()) {
            ClientAuthCert authCert = new ClientAuthCert();
            authCert.setIdentity(identityRepository.findById(1).orElseThrow());
            authCert.setIssuedAt(LocalDateTime.now().minusDays(43));
            authCert.setRevokedAt(LocalDateTime.now());
            authCert.setSerial(new BigInteger("200204320432"));
            clientAuthCertRepository.save(authCert);
            log.info("Added {} for testing...", authCert);

            authCert = new ClientAuthCert();
            authCert.setIdentity(identityRepository.findById(1).orElseThrow());
            authCert.setIssuedAt(LocalDateTime.now());
            authCert.setSerial(new BigInteger("20657043894378"));
            clientAuthCertRepository.save(authCert);
            log.info("Added {} for testing...", authCert);
        }
    }
}
