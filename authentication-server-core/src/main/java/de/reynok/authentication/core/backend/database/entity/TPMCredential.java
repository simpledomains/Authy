package de.reynok.authentication.core.backend.database.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class TPMCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(targetEntity = Identity.class, fetch = FetchType.LAZY)
    private Identity identity;

    private byte[] credential;
}
