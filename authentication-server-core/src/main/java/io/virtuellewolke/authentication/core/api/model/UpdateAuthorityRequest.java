package io.virtuellewolke.authentication.core.api.model;

import io.virtuellewolke.authentication.core.database.entity.Authority;

public class UpdateAuthorityRequest extends UpdateRequest<Authority> {
    @Override
    public Authority update(Authority source) {
        updateField(source, "name");
        return source;
    }
}