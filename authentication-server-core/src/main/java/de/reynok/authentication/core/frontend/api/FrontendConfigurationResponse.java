package de.reynok.authentication.core.frontend.api;

import de.reynok.authentication.core.frontend.configuration.FrontendConfiguration;
import lombok.Getter;

@Getter
public class FrontendConfigurationResponse {

    private final Boolean darkMode;

    public FrontendConfigurationResponse(FrontendConfiguration configuration) {
        this.darkMode = configuration.getDarkMode();
    }
}
