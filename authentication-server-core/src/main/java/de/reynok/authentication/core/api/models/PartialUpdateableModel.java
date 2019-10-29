package de.reynok.authentication.core.api.models;


import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Map;

@Slf4j
public class PartialUpdateableModel {

    public void updateFrom(Map<String, Object> data, boolean force) {
        data.forEach((k, v) -> {
            Field f = ReflectionUtils.findField(getClass(), k);

            if (f != null) {
                ReflectionUtils.makeAccessible(f);

                if (canUpdateField(f) || force) {
                    log.debug("Updating {} in Object {}", f.getName(), this);
                    onUpdateField(f, v);
                }
            }
        });
    }

    public void updateFrom(Map<String, Object> data) {
        updateFrom(data, false);
    }

    protected void onUpdateField(Field field, Object value) {
        ReflectionUtils.setField(field, this, value);
    }

    protected boolean canUpdateField(Field field) {
        return true;
    }
}
