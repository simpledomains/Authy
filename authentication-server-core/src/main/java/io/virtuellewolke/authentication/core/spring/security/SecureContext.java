package io.virtuellewolke.authentication.core.spring.security;

import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.database.entity.Service;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class SecureContext {
    public enum Source {
        API_TOKEN,
        COOKIE,
        CLIENT_CERT,
        WEBAUTHN
    }

    private Source   source = Source.COOKIE;
    private Identity identity;
    private Service  service;
}
