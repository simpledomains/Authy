package de.reynok.authentication.core.frontend.api;

import de.reynok.authentication.core.frontend.configuration.FrontendConfiguration;
import lombok.Getter;

@Getter
public class FrontendConfigurationResponse {

    private final Boolean darkMode;
    private final Boolean clientAuthCert;

    public FrontendConfigurationResponse(FrontendConfiguration configuration) {
        this.darkMode       = configuration.getDarkMode();
        this.clientAuthCert = configuration.getClientCertAuth();
    }
}
