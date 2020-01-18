package de.reynok.authentication.core.frontend.api;

import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AssertionStartResponse {

    private ByteArray                           challenge;
    private UserVerificationRequirement         userVerification;
    private AssertionExtensionInputs            extensions;
    private List<PublicKeyCredentialDescriptor> allowCredentials;
    private PublicKeyCredentialRequestOptions   publicKey;

    public AssertionStartResponse(AssertionRequest result) {
        this.challenge = result.getPublicKeyCredentialRequestOptions().getChallenge();
        this.userVerification = result.getPublicKeyCredentialRequestOptions().getUserVerification();
        this.extensions = result.getPublicKeyCredentialRequestOptions().getExtensions();
        this.allowCredentials = result.getPublicKeyCredentialRequestOptions().getAllowCredentials().orElseGet(ArrayList::new);

        this.publicKey = result.getPublicKeyCredentialRequestOptions();
    }
}