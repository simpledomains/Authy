package de.reynok.authentication.core.conf;

import de.reynok.authentication.core.api.models.Authority;
import de.reynok.authentication.core.api.models.Identity;
import de.reynok.authentication.core.api.models.Service;
import de.reynok.authentication.core.logic.database.repository.AuthorityRepository;
import de.reynok.authentication.core.logic.database.repository.IdentityRepository;
import de.reynok.authentication.core.logic.database.repository.ServiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class SystemInitializer {

    @Autowired
    private IdentityRepository identityRepository;
    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private ServiceRepository serviceRepository;

    @PostConstruct
    private void initialize() {

        if (serviceRepository.findAll().size() == 0) {
            Service applicationService = new Service();
            applicationService.setName("Authentication Service");
            applicationService.setMode(Service.ServiceMode.PUBLIC);
            applicationService.getAllowedUrls().add("/");
            applicationService.setEnabled(true);
            serviceRepository.save(applicationService);

            Service authedService = new Service();
            authedService.setName("Protect some app");
            authedService.getAllowedUrls().add("https://google.com*");
            authedService.setMode(Service.ServiceMode.AUTHORIZED);
            authedService.getRequiredRoles().add("admin");
            authedService.getRequiredRoles().add("cloud");
            authedService.getRequiredRoles().add("monitoring");
            authedService.getRequiredRoles().add("sysadmin");
            serviceRepository.save(authedService);

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
            identity.setApiToken("7KVEfk8dG7sxTGngUwpFAgyc89SHRR6jssCaeukDHUVSQbJx7exMR5GVBqdbGyyu");

            identity.getAuthorities().add(authorityRepository.findByName("admin").get());

            identityRepository.save(identity);

            log.info("Adding Administrator Account (admin/admin) cuz there is no admin ?!?.");
        }
    }
}
