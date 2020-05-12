package io.virtuellewolke.authentication.core.cas.model;


import io.virtuellewolke.authentication.core.database.entity.Identity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString(exclude = "identity")
public class Ticket implements Serializable {
    private String   token;
    private String   serviceUrl;
    private Identity identity;
}