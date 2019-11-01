package de.reynok.authentication.core.frontend.controller;


import de.reynok.authentication.core.Constants;
import de.reynok.authentication.core.shared.exceptions.AccessDeniedException;
import de.reynok.authentication.core.backend.database.entity.Identity;
import de.reynok.authentication.core.frontend.api.ServiceError;
import de.reynok.authentication.core.backend.database.repository.IdentityRepository;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

@Slf4j
public abstract class AbstractAuthyController {
    protected final IdentityRepository identityRepository;

    public AbstractAuthyController(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    protected Identity getIdentityFromRequest(HttpServletRequest request) {
        if (request.getAttribute(Constants.REQUEST_CLAIMS_FIELD) != null) {
            Claims claims = (Claims) request.getAttribute(Constants.REQUEST_CLAIMS_FIELD);

            if (claims != null) {
                log.info("{}", claims);

                return identityRepository.findByUsername(claims.get("sub").toString()).orElseThrow(EntityNotFoundException::new);
            }
        }

        throw new EntityNotFoundException();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity entityNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(404)
                .body(new ServiceError().setMessage(e.getMessage() != null ? e.getMessage() : "Entity not found."));
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity throwable(Throwable e) {
        log.error("Unknown error thrown, {}", e.getMessage(), e);
        return ResponseEntity.status(500)
                .body(new ServiceError().setMessage(e.getMessage() != null ? e.getMessage() : "A unknown error occurred."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity accessDenied(AccessDeniedException e) {
        log.debug("AccessDenied Exception thrown... {}", e.getMessage(), e);
        return ResponseEntity.status(e.getCode())
                .body(new ServiceError().setMessage(e.getMessage()));
    }
}