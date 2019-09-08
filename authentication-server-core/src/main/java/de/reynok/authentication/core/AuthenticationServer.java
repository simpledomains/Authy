package de.reynok.authentication.core;

import de.reynok.authentication.core.database.entity.Authority;
import de.reynok.authentication.core.database.entity.Identity;
import de.reynok.authentication.core.database.entity.Service;
import de.reynok.authentication.core.database.repository.AuthorityRepository;
import de.reynok.authentication.core.database.repository.IdentityRepository;
import de.reynok.authentication.core.database.repository.ServiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class AuthenticationServer {

    @Autowired
    private IdentityRepository identityRepository;
    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private ServiceRepository serviceRepository;

    public static void main(String[] args) {
        SpringApplication.run(AuthenticationServer.class, args);
    }

    @PostConstruct
    public void initAdmin() {

        if(serviceRepository.findAll().size() == 0) {
            Service applicationService = new Service();
            applicationService.setName("Authentication Service");
            applicationService.setMode(Service.ServiceMode.PUBLIC);
            applicationService.getAllowedUrls().add("/");
            applicationService.setEnabled(true);
            serviceRepository.save(applicationService);
        }

        Service service = new Service();
        service.setName("Allow all");
        service.getAllowedUrls().add("*");
        service.setMode(Service.ServiceMode.PUBLIC);
        serviceRepository.save(service);

        if(!authorityRepository.findByName("admin").isPresent()) {
            Authority adminRole = new Authority("admin");
            authorityRepository.save(adminRole);
        }
        if(!authorityRepository.findByName("gitlab").isPresent()) {
            Authority gitlabRole = new Authority("gitlab");
            authorityRepository.save(gitlabRole);
        }

        if(!identityRepository.findByUsername("admin").isPresent()) {
            Identity identity = new Identity();
            identity.setUsername("admin");
            identity.setPassword("admin");

            identity.getAuthorities().add(authorityRepository.findByName("admin").get());
            identity.getAuthorities().add(authorityRepository.findByName("gitlab").get());

            identityRepository.save(identity);

            log.info("Adding Administrator Account (admin/admin).");
        }
    }
}