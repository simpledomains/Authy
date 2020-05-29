package io.virtuellewolke.authentication.core.spring.components;

import io.jsonwebtoken.*;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.database.entity.Service;
import io.virtuellewolke.authentication.core.exceptions.SecurityTokenExpiredException;
import io.virtuellewolke.authentication.core.exceptions.SecurityTokenInvalidException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtProcessor {

    @Value("${app.secrets.jwt}")
    private String secret;

    public String getJwtTokenFor(Identity identity, Service service) {

        Map<String, Object> claims = new HashMap<>();

        claims.putIfAbsent("sub", identity.getUsername());
        claims.putIfAbsent("attributes", identity.getMetaData());
        claims.putIfAbsent("uid", identity.getId());
        claims.putIfAbsent("administrator", identity.getAdmin().toString());

        return Jwts.builder()
                .setHeaderParam("sub", identity.getUsername())
                .setClaims(claims)
                .setIssuer("Authy (" + (service != null ? service.getName() : "Unknown") + ")")
                .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 12)))
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, secret.getBytes(Charset.defaultCharset()))
                .compact();
    }

    public Claims validateToken(String token) throws SecurityTokenInvalidException {
        try {
            return Jwts.parser().setSigningKey(secret.getBytes(Charset.defaultCharset()))
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException | IllegalArgumentException e) {
            throw new SecurityTokenInvalidException(e);
        } catch (ExpiredJwtException e) {
            throw new SecurityTokenExpiredException("JWT expired", e);
        }
    }
}