package io.virtuellewolke.authentication.core.spring.components;

import io.virtuellewolke.authentication.core.database.entity.Service;
import io.virtuellewolke.authentication.core.database.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ServiceValidation {
    private final ServiceRepository serviceRepository;

    public Service getRegisteredServiceFor(String serviceUrl) {
        Service       result      = null;
        List<Service> serviceList = serviceRepository.findAll();

        log.debug("isAllowed '{}' in {}", serviceUrl, serviceList);

        if (serviceList != null) {
            Iterator<Service> serviceIterator = serviceList.iterator();

            while (serviceIterator.hasNext() && result == null) {
                Service item = serviceIterator.next();

                if (item.getEnabled()) {
                    if (item.getAllowedUrls().contains(serviceUrl)) {
                        result = item;
                    } else {
                        for (String allowedRoute : item.getAllowedUrls()) {
                            Pattern p = Pattern.compile("^" + allowedRoute.replaceAll("/", "\\/").replaceAll("\\*", "(.*)") + "$");
                            if (p.matcher(serviceUrl).matches()) {
                                result = item;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }
}