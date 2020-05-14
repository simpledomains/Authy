package io.virtuellewolke.authentication.core.api.service;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.virtuellewolke.authentication.core.api.model.UIInformation;
import io.virtuellewolke.authentication.core.spring.security.annotations.AuthorizedResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/api/ui", produces = "application/json")
@Tag(name = "Authy - UI Resource")
public interface UIResource {

    @AuthorizedResource
    @GetMapping("/info")
    ResponseEntity<UIInformation> getUiInfo();
}