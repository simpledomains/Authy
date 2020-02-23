package io.virtuellewolke.authentication.core.impl.service;

import de.reynok.authentication.core.backend.modules.cas.TicketType;
import de.reynok.authentication.core.frontend.api.LoginRequest;
import de.reynok.authentication.core.frontend.api.LoginResponse;
import de.reynok.authentication.core.shared.exceptions.AccessDeniedException;
import de.reynok.authentication.core.shared.exceptions.SecurityTokenExpiredException;
import io.virtuellewolke.authentication.core.api.model.cas.AuthFailedResponse;
import io.virtuellewolke.authentication.core.api.model.cas.AuthResponse;
import io.virtuellewolke.authentication.core.api.model.cas.AuthSuccessResponse;
import io.virtuellewolke.authentication.core.api.service.CASResource;
import io.virtuellewolke.authentication.core.cas.TicketManager;
import io.virtuellewolke.authentication.core.cas.model.Ticket;
import io.virtuellewolke.authentication.core.database.repository.IdentityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
public class CASResourceImpl implements CASResource {

    private final TicketManager      ticketManager;
    private final IdentityRepository identityRepository;

    @Override
    public ResponseEntity<AuthResponse> validate(HttpServletRequest request, String token, String service) {
        try {
            Ticket ticket = ticketManager.getTicket(token, service);

            AuthSuccessResponse success = new AuthSuccessResponse();
            success.setUser(ticket.getIdentity().getUsername());

            ticket.getIdentity().getAuthorities().forEach(authority -> success.addAttribute(request, "role", authority.getName()));
            ticket.getIdentity().getMetaData().forEach((key, value) -> success.addAttribute(request, key, value));

            success.addAttribute(request, "displayName", ticket.getIdentity().getDisplayName());
            success.addAttribute(request, "email", ticket.getIdentity().getEmail());
            success.addAttribute(request, "locked", ticket.getIdentity().getLocked().toString());
            success.addAttribute(request, "admin", ticket.getIdentity().getAdmin().toString());
            success.addAttribute(request, "otpEnabled", ticket.getIdentity().getOtpEnabled().toString());

            return ResponseEntity.status(HttpStatus.OK).body(new AuthResponse(success));
        } catch (AccessDeniedException | SecurityTokenExpiredException e) {
            String newToken = ticketManager.issue(TicketType.ST, "/", identityRepository.findById(1).orElseThrow()).getToken();

            AuthFailedResponse failedResponse = new AuthFailedResponse();
            failedResponse.setCode(AuthFailedResponse.ErrorCode.INVALID_TICKET);
            failedResponse.setValue(e.getMessage() + " (Try " + newToken + " with Service '/')");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(failedResponse));
        }
    }

    @Override
    public ResponseEntity<LoginResponse> login(HttpServletResponse response, LoginRequest body, String serviceUrl) {
        return null;
    }
}
