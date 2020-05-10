package io.virtuellewolke.authentication.core.api.model.cas;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Setter
@Getter
public class AuthSuccessResponse {

    @JacksonXmlProperty(localName = "cas:user")
    private String              user;
    @JacksonXmlProperty(localName = "cas:attributes")
    private Map<String, Object> attributes = new HashMap<>();


    //region Multimap Values

    @Hidden
    @JsonIgnore
    private Multimap<String, Object> _attributes = HashMultimap.create();

    public void addAttribute(HttpServletRequest request, String key, String value) {
        if (value == null) return;

        if (request != null && (request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/xml"))) {
            this._attributes.put(String.format("cas:%s", key), value);
        } else {
            this._attributes.put(key, value);
        }

        this.attributes = _attributes.asMap().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, val -> {
                    if (val.getValue().size() == 1) return val.getValue().iterator().next();
                    return val.getValue();
                }));
    }

    //endregion
}