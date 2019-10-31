package de.reynok.authentication.core.api.service;

import de.reynok.authentication.core.conf.FrontendConfiguration;
import lombok.Getter;

@Getter
public class FrontendConfigurationResponse {

    private final Boolean darkMode;

    public FrontendConfigurationResponse(FrontendConfiguration configuration) {
        this.darkMode = configuration.getDarkMode();
    }
}
