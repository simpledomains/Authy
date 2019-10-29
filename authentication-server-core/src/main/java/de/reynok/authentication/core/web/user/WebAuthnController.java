package de.reynok.authentication.core.web.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import de.reynok.authentication.core.Constants;
import de.reynok.authentication.core.annotations.WebRequiresAuthentication;
import de.reynok.authentication.core.api.exception.AccessDeniedException;
import de.reynok.authentication.core.api.models.Identity;
import de.reynok.authentication.core.api.models.TPMCredential;
import de.reynok.authentication.core.api.service.webauthn.AssertionStartResponse;
import de.reynok.authentication.core.conf.CASConfiguration;
import de.reynok.authentication.core.logic.database.repository.IdentityRepository;
import de.reynok.authentication.core.logic.database.repository.TPMCredentialRepository;
import de.reynok.authentication.core.util.JwtProcessor;
import de.reynok.authentication.core.web.RequestProcessedController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
//@RestController
//@RequestMapping("/webauthn")
public class WebAuthnController extends RequestProcessedController {

    private Map<Identity, PublicKeyCredentialCreationOptions> registrationCache = new HashMap<>();
    private Map<String, AssertionRequest>                     assertionCache    = new HashMap<>();

    @Autowired
    private JwtProcessor            jwtProcessor;
    @Autowired
    private CASConfiguration        casConfiguration;
    @Autowired
    private RelyingParty            relyingParty;
    @Autowired
    private TPMCredentialRepository credentialRepository;

    private SecureRandom secureRandom;

    public WebAuthnController(IdentityRepository identityRepository) {
        super(identityRepository);
        this.secureRandom = new SecureRandom();
    }

    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

        builder.featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        builder.serializationInclusion(JsonInclude.Include.NON_ABSENT);
        builder.modules(new Jdk8Module());

        return builder;
    }

    @WebRequiresAuthentication
    @PostMapping("/registration/start")
    public PublicKeyCredentialCreationOptions registrationStart(HttpServletRequest httpRequest, @RequestParam(value = "name", required = false) String deviceName) {
        Identity identity = getIdentityFromRequest(httpRequest);

        if (deviceName == null) {
            deviceName = "Unknown Device";
        }

        byte[] userHandle = new byte[64];
        secureRandom.nextBytes(userHandle);

        PublicKeyCredentialCreationOptions request = relyingParty.startRegistration(StartRegistrationOptions.builder()
                .user(UserIdentity.builder()
                        .name(identity.getUsername())
                        .displayName(deviceName)
                        .id(new ByteArray(userHandle))
                        .build())
                .build()
        );

        registrationCache.put(identity, request);


        return request;
    }

    @PostMapping("/registration/finish")
    public RegistrationResult registrationFinish(HttpServletRequest httpRequest, @RequestBody PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc) throws IOException, RegistrationFailedException {
        Identity identity = getIdentityFromRequest(httpRequest);

        try {
            RegistrationResult registrationResult = relyingParty.finishRegistration(FinishRegistrationOptions.builder()
                    .request(registrationCache.get(identity)).response(pkc).build());

            registrationCache.remove(identity);

            TPMCredential credential = new TPMCredential();
            credential.setIdentity(identity);
            credential.setCredential(registrationResult.getPublicKeyCose().getBytes());

            credentialRepository.save(credential);

            return registrationResult;
        } catch (RegistrationFailedException e) {
            log.error("Failed to register U2F device for Identity {}", identity);
            throw e;
        }
    }

    @PostMapping("/assertion/start")
    public AssertionRequest assertionStart(HttpServletRequest httpRequest, @RequestParam("username") String username) {
        AssertionRequest request = relyingParty.startAssertion(StartAssertionOptions.builder()
                .username(username)
                .userVerification(UserVerificationRequirement.PREFERRED)
                .build()
        );

        assertionCache.put(httpRequest.getRemoteAddr(), request);

        return request;
    }

    @PostMapping("/assertion/finish")
    public AssertionResult assertionFinish(HttpServletRequest httpRequest, HttpServletResponse response, @RequestBody PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc) throws IOException, AccessDeniedException, AssertionFailedException {
        try {
            AssertionResult result = relyingParty.finishAssertion(FinishAssertionOptions.builder()
                    .request(assertionCache.get(httpRequest.getRemoteAddr())).response(pkc).build());

            if (result.isSuccess()) {

                Identity identity = identityRepository.findByUsername(result.getUsername()).orElseThrow(EntityNotFoundException::new);

                Cookie cookie = new Cookie(Constants.COOKIE_NAME, jwtProcessor.getJwtTokenFor(identity, null));
                cookie.setMaxAge(casConfiguration.getCookieMaxAge());
                cookie.setPath(casConfiguration.getCookiePath());
                cookie.setComment(casConfiguration.getCookieComment());

                if (casConfiguration.getCookieDomain() != null) {
                    cookie.setDomain(casConfiguration.getCookieDomain());
                }

                response.addCookie(cookie);

                return result;
            } else {
                throw new AccessDeniedException();
            }
        } catch (AssertionFailedException e) {
            log.error("Failed to assert U2F device.", e);
            throw e;
        }
    }
}