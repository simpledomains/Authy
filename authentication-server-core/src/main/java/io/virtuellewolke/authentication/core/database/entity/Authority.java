package io.virtuellewolke.authentication.core.database.entity;

import io.virtuellewolke.authentication.core.util.PartialUpdateableModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@ToString
public class Authority extends PartialUpdateableModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String  name;

    public Authority() {
    }

    public Authority(String name) {
        this.name = name;
    }
}
