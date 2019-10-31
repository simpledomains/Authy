package de.reynok.authentication.core.web;

import de.reynok.authentication.core.api.service.FrontendConfigurationResponse;
import de.reynok.authentication.core.conf.FrontendConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FrontendInfoController {
    @Autowired
    private FrontendConfiguration frontendConfiguration;

    @GetMapping("/api/frontend/config")
    public FrontendConfigurationResponse getFrontendConfiguration() {
        return new FrontendConfigurationResponse(frontendConfiguration);
    }
}