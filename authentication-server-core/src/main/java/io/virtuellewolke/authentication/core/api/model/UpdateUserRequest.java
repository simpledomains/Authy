package io.virtuellewolke.authentication.core.api.model;

import io.virtuellewolke.authentication.core.database.entity.Identity;

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
        return source;
    }
}