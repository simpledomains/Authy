package de.reynok.authentication.core;

import de.reynok.authentication.core.backend.database.entity.Authority;
import de.reynok.authentication.core.backend.database.entity.Identity;
import de.reynok.authentication.core.backend.database.entity.Service;
import de.reynok.authentication.core.backend.database.repository.AuthorityRepository;
import de.reynok.authentication.core.backend.database.repository.IdentityRepository;
import de.reynok.authentication.core.backend.database.repository.ServiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class AuthenticationServerInit {

    @Autowired
    private IdentityRepository  identityRepository;
    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private ServiceRepository   serviceRepository;

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
            identity.setOtpSecret("JBSWY3DPEHPK3PXP");

            identity.getAuthorities().add(authorityRepository.findByName("admin").get());

            identityRepository.save(identity);

            log.info("Adding Administrator Account (admin/admin) cuz there is no admin ?!?.");
        }
    }
}
