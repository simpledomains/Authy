package io.virtuellewolke.authentication.core.api.model;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
@Setter
public abstract class UpdateRequest<T> {
    private Map<String, Object> data;

    public abstract T update(T source);

    protected void updateField(T object, String key) {
        updateField(object, key, (t, value) -> {
            PropertyAccessor accessor = PropertyAccessorFactory.forBeanPropertyAccess(object);
            accessor.setPropertyValue(key, value);
        });
    }

    protected void updateField(T object, String key, BiConsumer<T, Object> consumer) {
        if (object != null && data != null && data.containsKey(key)) {
            log.trace("Updating {} {}->{}", object.getClass().getSimpleName(), key, data.get(key));
            consumer.accept(object, data.get(key));
        }
    }
}