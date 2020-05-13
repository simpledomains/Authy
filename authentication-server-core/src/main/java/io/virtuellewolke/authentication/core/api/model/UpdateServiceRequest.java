package io.virtuellewolke.authentication.core.api.model;

import io.virtuellewolke.authentication.core.database.entity.Service;

public class UpdateServiceRequest extends UpdateRequest<Service> {
    @Override
    public Service update(Service source) {
        updateField(source, "name");
        updateField(source, "enabled", (service, o) -> {
            if (o instanceof String) {
                service.setEnabled(Boolean.parseBoolean(o.toString()));
            }
        });
        updateField(source, "mode", (service, o) -> service.setMode(Service.ServiceMode.valueOf(o.toString())));
        return source;
    }
}