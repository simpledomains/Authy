package io.virtuellewolke.authentication.core.api.model;

import io.virtuellewolke.authentication.core.database.entity.Service;

import java.util.List;

public class UpdateServiceRequest extends UpdateRequest<Service> {
    @Override
    public Service update(Service source) {
        updateField(source, "name");
        updateField(source, "enabled", (service, o) -> {
            if (o instanceof String) {
                service.setEnabled(Boolean.parseBoolean(o.toString()));
            } else if (o instanceof Boolean) {
                service.setEnabled((Boolean) o);
            }
        });
        updateField(source, "allowedUrls", ((service, o) -> {
            if (o instanceof List) {
                //noinspection unchecked
                source.setAllowedUrls((List<String>) o);
            }
        }));
        updateField(source, "requiredRoles", ((service, o) -> {
            if (o instanceof List) {
                //noinspection unchecked
                source.setRequiredRoles((List<String>) o);
            }
        }));
        updateField(source, "mode", (service, o) -> service.setMode(Service.ServiceMode.valueOf(o.toString())));
        return source;
    }
}