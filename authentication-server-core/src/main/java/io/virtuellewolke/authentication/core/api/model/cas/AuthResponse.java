package io.virtuellewolke.authentication.core.api.model.cas;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JacksonXmlRootElement(localName = "cas:serviceResponse")
public class AuthResponse {
    @JacksonXmlProperty(localName = "cas:authenticationSuccess")
    private AuthSuccessResponse authenticationSuccess;
    @JacksonXmlProperty(localName = "cas:authenticationFailure")
    private AuthFailedResponse  authenticationFailure;

    public AuthResponse(AuthSuccessResponse successResponse) {
        this.authenticationSuccess = successResponse;
    }

    public AuthResponse(AuthFailedResponse failedResponse) {
        this.authenticationFailure = failedResponse;
    }
}