package io.virtuellewolke.authentication.core;

import io.virtuellewolke.authentication.core.database.entity.Authority;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.database.entity.Service;
import io.virtuellewolke.authentication.core.database.repository.AuthorityRepository;
import io.virtuellewolke.authentication.core.database.repository.ClientAuthCertRepository;
import io.virtuellewolke.authentication.core.database.repository.IdentityRepository;
import io.virtuellewolke.authentication.core.database.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AuthenticationServerInit implements InitializingBean {

    private final IdentityRepository       identityRepository;
    private final AuthorityRepository      authorityRepository;
    private final ServiceRepository        serviceRepository;
    private final ClientAuthCertRepository clientAuthCertRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (serviceRepository.findAll().size() == 0) {
            Service applicationService = new Service();
            applicationService.setName("Authentication Service");
            applicationService.setMode(Service.ServiceMode.PUBLIC);
            applicationService.getAllowedUrls().add("/");
            applicationService.setEnabled(true);
            serviceRepository.save(applicationService);

            log.info("Added Service {}", applicationService);

            Service service = new Service();
            service.setName("Allow all");
            service.setEnabled(false);
            service.getAllowedUrls().add("*");
            service.setMode(Service.ServiceMode.PUBLIC);
            serviceRepository.save(service);

            log.info("Added Service {}", service);
        }


        if (authorityRepository.findByName("admin").isEmpty()) {
            Authority adminRole = new Authority("admin");
            authorityRepository.save(adminRole);

            log.info("Added Authority {}", adminRole);
        }

        if (identityRepository.findByAdmin(true).size() == 0) {
            Identity identity = new Identity();
            identity.setUsername("admin");
            identity.setPassword("admin");
            identity.setAdmin(true);
            identity.setEmail("admin@example.com");
            identity.setDisplayName("Administrator");
            identity.setOtpSecret("JBSWY3DPEHPK3PXP");
            identity.setApiToken(RandomStringUtils.randomAlphanumeric(32));

            identity.getAuthorities().add(authorityRepository.findByName("admin").get());

            identityRepository.save(identity);

            log.warn("Administrator-Account was added (username='admin', password='admin', apiToken='{}')", identity.getApiToken());
        }
    }
}
