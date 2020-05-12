package io.virtuellewolke.authentication.core.api.model.cas;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.virtuellewolke.authentication.core.spring.configuration.ObjectMapperConfiguration;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JacksonXmlRootElement(localName = "cas:serviceResponse")
public class AuthResponse {

    @ObjectMapperConfiguration.JacksonJsonIgnore
    @JacksonXmlProperty(isAttribute = true, localName = "xmlns:cas")
    private String namespace = "http://www.yale.edu/tp/cas";

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