package de.reynok.authentication.core.web;


import de.reynok.authentication.core.Constants;
import de.reynok.authentication.core.database.entity.Identity;
import de.reynok.authentication.core.database.repository.IdentityRepository;
import de.reynok.authentication.core.web.api.RestError;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RequiredArgsConstructor
public class RequestProcessedController {
    protected final IdentityRepository identityRepository;

    protected Identity getIdentityFromRequest(HttpServletRequest request) {
        if (request.getAttribute(Constants.REQUEST_CLAIMS_FIELD) != null) {
            Claims claims = (Claims) request.getAttribute(Constants.REQUEST_CLAIMS_FIELD);

            log.info("{}", claims);

            return identityRepository.findByUsername(claims.get("sub").toString()).orElseThrow(EntityNotFoundException::new);
        }

        throw new EntityNotFoundException();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity entityNotFound(EntityNotFoundException e) {
        return new RestError(e, e.getMessage() != null ? e.getMessage() : "Entity not found.", 404).toResponse();
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity throwable(Throwable e) {
        return new RestError(e, e.getMessage() != null ? e.getMessage() : "A unknown error occurred.", 500).toResponse();
    }
}