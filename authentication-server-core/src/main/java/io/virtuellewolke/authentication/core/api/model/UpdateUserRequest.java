package io.virtuellewolke.authentication.core.api.model;

import io.virtuellewolke.authentication.core.database.entity.Authority;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class UpdateUserRequest extends UpdateRequest<Identity> {
    @Override
    public Identity update(Identity source) {
        updateField(source, "username");
        updateField(source, "password", (identity, o) -> identity.setPassword(o.toString()));
        updateField(source, "displayName");
        updateField(source, "email");
        updateField(source, "avatar");
        updateField(source, "apiToken");
        updateField(source, "otpSecret");
        updateField(source, "locked");
        updateField(source, "admin", ((identity, o) -> identity.setAdmin(o.toString().toLowerCase().equals("true"))));

        if (data.containsKey("authorities")) {
            if (data.get("authorities") instanceof List) {
                //noinspection unchecked
                List<Map<String, Object>> roles = (List<Map<String, Object>>) data.get("authorities");

                List<Authority> authorities = roles.stream().map(e -> {
                    Authority a = new Authority();
                    a.setId((Integer) e.get("id"));
                    return a;
                }).collect(Collectors.toList());

                source.setAuthorities(authorities);
                log.trace("Updating {} {}->{}", source.getClass().getSimpleName(), "authorities", source.getAuthorities());
            }
        }

        return source;
    }
}