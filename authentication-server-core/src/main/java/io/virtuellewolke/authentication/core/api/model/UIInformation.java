package io.virtuellewolke.authentication.core.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UIInformation {
    @JsonProperty("mTLSEnabled")
    private Boolean mTLSEnabled;
}
