package de.reynok.authentication.core.backend.database.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import javax.persistence.*;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ClientAuthCert {
    @Id
    private BigInteger serial;

    @ManyToOne(targetEntity = Identity.class, fetch = FetchType.LAZY)
    private Identity identity;

    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    private LocalDateTime issuedAt;
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    private LocalDateTime revokedAt;
    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    private LocalDateTime lastAccess;

    public boolean isRevoked() {
        return revokedAt != null && LocalDateTime.now().isAfter(revokedAt);
    }
}