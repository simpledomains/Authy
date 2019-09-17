package de.reynok.authentication.core.cas;

import de.reynok.authentication.core.database.entity.Identity;
import de.reynok.authentication.core.database.entity.Service;
import de.reynok.authentication.core.database.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ServiceValidation {
    private final ServiceRepository serviceRepository;

    public Service isAllowed(String serviceUrl) {
        Service result = null;

        for (Service item : serviceRepository.findAll()) {
            if (item.getAllowedUrls().contains(serviceUrl)) {
                result = item;
            } else {
                for (String allowedRoute: item.getAllowedUrls()) {
                    Pattern p = Pattern.compile("^" + allowedRoute.replaceAll("/", "\\/").replaceAll("\\*", "(.*)") + "$");
                    if (p.matcher(serviceUrl).matches()) {
                        result = item;
                    }
                }
            }
        }

        return result;
    }
}