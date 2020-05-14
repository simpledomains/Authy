package io.virtuellewolke.authentication.core.api.model.cas;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AuthFailedResponse {

    public enum ErrorCode {
        INVALID_REQUEST,
        INVALID_TICKET_SPEC,
        UNAUTHORIZED_SERVICE_PROXY,
        INVALID_PROXY_CALLBACK,
        INVALID_TICKET,
        INVALID_SERVICE,
        INTERNAL_ERROR,
        AUTHORIZATION_DENIED,
    }

    @JacksonXmlProperty(isAttribute = true, localName = "code")
    private ErrorCode code;
    @JacksonXmlText
    @JsonProperty("description")
    private String    value;
}
