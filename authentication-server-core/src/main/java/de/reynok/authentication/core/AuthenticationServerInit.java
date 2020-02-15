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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AuthenticationServerInit implements InitializingBean {

    private final IdentityRepository       identityRepository;
    private final AuthorityRepository      authorityRepository;
    private final ServiceRepository        serviceRepository;
    private final ClientAuthCertRepository clientAuthCertRepository;

    @Value("${cas.test-mode:#{false}}")
    private Boolean testMode = false;

    @Override
    public void afterPropertiesSet() throws Exception {

        if (testMode) {
            log.warn("TEST_MODE ENABLED!");
        }

        if (serviceRepository.findAll().size() == 0) {
            Service applicationService = new Service();
            applicationService.setName("Authentication Service");
            applicationService.setMode(Service.ServiceMode.PUBLIC);
            applicationService.getAllowedUrls().add("/");
            applicationService.setEnabled(true);
            serviceRepository.save(applicationService);

            Service service = new Service();
            service.setName("Allow all");
            service.setEnabled(false);
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

            identity.getAuthorities().add(authorityRepository.findByName("admin").get());

            identityRepository.save(identity);

            log.info("Adding Administrator Account (admin/admin) cuz there is no admin ?!?.");
        }

        if (testMode && clientAuthCertRepository.findAll().size() == 0) {
            ClientAuthCert cert = new ClientAuthCert();
            cert.setIssuedAt(LocalDateTime.now());
            cert.setIdentity(identityRepository.findByAdmin(true).get(0));
            cert.setSerial(new BigInteger("12324732957948"));
            cert.setName("Test Certificate (ac)");

            clientAuthCertRepository.save(cert);
        }

        if (testMode && serviceRepository.findAll().size() == 2) {
            Service service = new Service();
            service.setName("Authenticated Test");
            service.setEnabled(true);
            service.getAllowedUrls().add("https://[A-z]+.r3ktm8.de/*");
            service.setMode(Service.ServiceMode.AUTHORIZED);
            service.getRequiredRoles().add("admin");
            service.getRequiredRoles().add("monitoring");
            service.getRequiredRoles().add("family");
            service.getRequiredRoles().add("system_admin");
            service.getRequiredRoles().add("root");
            service.getRequiredRoles().add("root2");
            service.getRequiredRoles().add("root3");
            service.getRequiredRoles().add("root4");
            serviceRepository.save(service);

            service = new Service();
            service.setName("Admin Only");
            service.setEnabled(true);
            service.getAllowedUrls().add("https://[A-z]+.seatech.dev/*");
            service.setMode(Service.ServiceMode.ADMIN);
            serviceRepository.save(service);

            service = new Service();
            service.setName("Anonymous.");
            service.setEnabled(false);
            service.getAllowedUrls().add("http://*");
            service.setMode(Service.ServiceMode.ANONYMOUS);
            serviceRepository.save(service);
        }
    }
}
