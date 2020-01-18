package de.reynok.authentication.core.frontend.api;

import com.yubico.webauthn.AssertionResult;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssertionServiceResult {
    public AssertionServiceResult(AssertionResult result) {
        
    }
}
