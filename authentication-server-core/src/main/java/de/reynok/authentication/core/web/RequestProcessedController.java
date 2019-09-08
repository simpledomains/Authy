package de.reynok.authentication.core.web;


import de.reynok.authentication.core.configuration.Constants;
import de.reynok.authentication.core.database.entity.Identity;
import de.reynok.authentication.core.database.repository.IdentityRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
public class RequestProcessedController {
    protected final IdentityRepository identityRepository;

    Identity getIdentityFromRequest(HttpServletRequest request) {
        if (request.getAttribute(Constants.REQUEST_CLAIMS_FIELD) != null) {
            Claims claims = (Claims) request.getAttribute(Constants.REQUEST_CLAIMS_FIELD);

            return identityRepository.findByUsername(claims.get("sub").toString()).orElseThrow(EntityNotFoundException::new);
        }

        throw new EntityNotFoundException();
    }
}