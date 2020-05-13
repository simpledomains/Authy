package io.virtuellewolke.authentication.core.api.service;

import io.virtuellewolke.authentication.core.api.model.UIInformation;
import io.virtuellewolke.authentication.core.spring.configuration.X509ManagerConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UIResourceImpl implements UIResource {

    private final X509ManagerConfiguration x509config;

    @Override
    public ResponseEntity<UIInformation> getUiInfo() {
        UIInformation uiInformation = new UIInformation();

        uiInformation.setMTLSEnabled(x509config.getEnabled());

        return ResponseEntity.ok(uiInformation);
    }
}
