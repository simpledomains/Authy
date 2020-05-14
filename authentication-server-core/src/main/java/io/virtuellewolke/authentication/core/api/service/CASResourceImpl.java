package io.virtuellewolke.authentication.core.api.service;

import io.virtuellewolke.authentication.core.api.Constants;
import io.virtuellewolke.authentication.core.api.LoginFailedException;
import io.virtuellewolke.authentication.core.api.model.LoginRequest;
import io.virtuellewolke.authentication.core.api.model.LoginResponse;
import io.virtuellewolke.authentication.core.api.model.cas.AuthFailedResponse;
import io.virtuellewolke.authentication.core.api.model.cas.AuthResponse;
import io.virtuellewolke.authentication.core.api.model.cas.AuthSuccessResponse;
import io.virtuellewolke.authentication.core.cas.StatusCode;
import io.virtuellewolke.authentication.core.cas.TicketManager;
import io.virtuellewolke.authentication.core.cas.TicketType;
import io.virtuellewolke.authentication.core.cas.model.Ticket;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.database.entity.Service;
import io.virtuellewolke.authentication.core.database.repository.IdentityRepository;
import io.virtuellewolke.authentication.core.exceptions.AccessDeniedException;
import io.virtuellewolke.authentication.core.exceptions.SecurityTokenExpiredException;
import io.virtuellewolke.authentication.core.spring.components.JwtProcessor;
import io.virtuellewolke.authentication.core.spring.components.LoginSecurity;
import io.virtuellewolke.authentication.core.spring.components.ServiceValidation;
import io.virtuellewolke.authentication.core.spring.configuration.CasConfiguration;
import io.virtuellewolke.authentication.core.spring.helper.SecureContextRequestHelper;
import io.virtuellewolke.authentication.core.spring.security.SecureContext;
import io.virtuellewolke.authentication.core.util.validation.Md5PasswordValidator;
import io.virtuellewolke.authentication.core.util.validation.OneTimePasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CASResourceImpl implements CASResource {

    private final CasConfiguration   configuration;
    private final TicketManager      ticketManager;
    private final IdentityRepository identityRepository;
    private final ServiceValidation  serviceValidation;
    private final JwtProcessor       jwtProcessor;
    private final LoginSecurity      loginSecurity;

    @Override
    public ResponseEntity<AuthResponse> validate(HttpServletRequest request, String token, String service) {
        log.trace("Accept-Header: {}", request.getHeader("Accept"));
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
            AuthFailedResponse failedResponse = new AuthFailedResponse();
            failedResponse.setCode(AuthFailedResponse.ErrorCode.INVALID_TICKET);
            failedResponse.setValue(e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(failedResponse));
        }
    }

    @Override
    public ResponseEntity<LoginResponse> login(HttpServletRequest req, HttpServletResponse response, LoginRequest login, String serviceUrl) {
        Identity identity = identityRepository.findByUsername(login.getUsername()).orElse(null);
        Service  service  = serviceValidation.getRegisteredServiceFor(serviceUrl);

        if (service == null) {
            throw new LoginFailedException(LoginResponse.ErrorCode.SERVICE_NOT_ALLOWED);
        }

        if (!loginSecurity.isAllowedToTry(req.getRemoteAddr())) {
            throw new LoginFailedException(LoginResponse.ErrorCode.USER_ACCOUNT_BLOCKED);
        }

        if (identity != null) {
            if (identity.getLocked()) {
                throw new LoginFailedException(LoginResponse.ErrorCode.USER_ACCOUNT_BLOCKED);
            }

            Md5PasswordValidator validator = new Md5PasswordValidator(identity);

            if (validator.isNotValid(login.getPassword())) {
                loginSecurity.recordFailedAttempt(req.getRemoteAddr());
                throw new LoginFailedException(LoginResponse.ErrorCode.CREDENTIAL_ERROR);
            }

            if (identity.getOtpEnabled()) {
                if (StringUtils.isBlank(login.getSecurityPassword())) {
                    throw new LoginFailedException(LoginResponse.ErrorCode.OTP_REQUIRED);
                } else {
                    OneTimePasswordValidator otpValidator = new OneTimePasswordValidator(identity.getOtpSecret());

                    if (otpValidator.isNotValid(login.getSecurityPassword())) {
                        loginSecurity.recordFailedAttempt(req.getRemoteAddr());
                        throw new LoginFailedException(LoginResponse.ErrorCode.CREDENTIAL_ERROR);
                    }
                }
            }

            if (service.isIdentityNotAllowed(identity)) {
                throw new LoginFailedException(LoginResponse.ErrorCode.USER_ACCOUNT_DENIED);
            }
        } else {
            loginSecurity.recordFailedAttempt(req.getRemoteAddr());
            throw new LoginFailedException(LoginResponse.ErrorCode.CREDENTIAL_ERROR);
        }

        String token = issueCookie(response, identity, service);

        loginSecurity.resetAttempts(req.getRemoteAddr());

        return ResponseEntity.ok()
                .body(LoginResponse.builder()
                        .location(getRedirectLogin(serviceUrl, identity))
                        .message("OK")
                        .token(token)
                        .build()
                );
    }

    // TODO: multiple returns...
    @Override
    public ResponseEntity<LoginResponse> loginPage(HttpServletRequest request, String serviceUrl) {
        if (SecureContextRequestHelper.hasSecureContext(request)) {
            SecureContext ctx = SecureContextRequestHelper.getSecureContext(request);
            assert ctx != null;

            Identity identity = ctx.getIdentity();
            Service  service  = serviceValidation.getRegisteredServiceFor(serviceUrl);

            if (service != null && service.getEnabled()) {
                if (service.isIdentityAllowed(identity)) {
                    String redirectUrl = getRedirectLogin(serviceUrl, identity);
                    return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                            .header("Location", redirectUrl)
                            .body(LoginResponse.builder().location(redirectUrl).message("OK").build());
                } else {
                    return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(URI.create("/#/error?code=" + StatusCode.DENIED + "&service=" + serviceUrl)).build();
                }
            }
        }

        Service service = serviceValidation.getRegisteredServiceFor(serviceUrl);

        if (service == null || !service.getEnabled()) {
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(URI.create("/#/error?code=" + StatusCode.INVALID_SERVICE + "&service=" + serviceUrl)).build();
        }

        if (serviceUrl.equals("/")) {
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(URI.create("/#/login?service=" + serviceUrl)).build();
        } else {
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(URI.create("/#/cas/login?service=" + serviceUrl)).build();
        }
    }

    @Override
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(Constants.COOKIE_NAME, "");
        cookie.setMaxAge(1);
        cookie.setPath(configuration.getCookiePath());
        if (configuration.getCookieDomain() != null) { cookie.setDomain(configuration.getCookieDomain()); }

        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> getLogoutPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(URI.create("/#/logout")).build();
    }

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<LoginResponse> handleLoginFailedException(LoginFailedException e) {
        if (e.getErrorCode() != null) {
            if (e.getErrorCode().equals(LoginResponse.ErrorCode.OTP_REQUIRED)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        LoginResponse.builder().errorCode(e.getErrorCode()).build()
                );
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        LoginResponse.builder().errorCode(e.getErrorCode()).build()
                );
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String issueCookie(HttpServletResponse response, Identity identity, Service service) {
        Cookie cookie = new Cookie(Constants.COOKIE_NAME, jwtProcessor.getJwtTokenFor(
                identity, service
        ));
        cookie.setMaxAge(configuration.getCookieLifeTime());
        cookie.setPath(configuration.getCookiePath());
        cookie.setComment("Authy CAS Token");

        if (configuration.getCookieDomain() != null) { cookie.setDomain(configuration.getCookieDomain()); }

        response.addCookie(cookie);

        return cookie.getValue();
    }

    private String getRedirectLogin(String service, Identity identity) {
        String serviceUrl = service;

        if (serviceUrl.contains("?")) {
            serviceUrl += "&";
        } else {
            serviceUrl += "?";
        }

        serviceUrl += "ticket=" + ticketManager.issue(TicketType.ST, service, identity).getToken();
        return serviceUrl;
    }
}
