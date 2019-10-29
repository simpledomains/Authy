package de.reynok.authentication.core.logic.cas;

import de.reynok.authentication.core.api.models.Service;
import de.reynok.authentication.core.logic.database.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ServiceValidation {
    private final ServiceRepository serviceRepository;

    public Service isAllowed(String serviceUrl) {
        Service result = null;
        List<Service> serviceList = serviceRepository.findAll();

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