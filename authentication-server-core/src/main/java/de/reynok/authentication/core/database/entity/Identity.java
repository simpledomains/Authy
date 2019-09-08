package de.reynok.authentication.core.database.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.reynok.authentication.core.security.validation.Md5PasswordValidator;
import de.reynok.authentication.core.security.validation.ValidatorChain;
import de.reynok.authentication.core.util.HashMapConverter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.codec.digest.Md5Crypt;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@ToString(exclude = {"password", "oneTimePassword"})
@Table(name = "identity")
public class Identity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer             id;
    @Column(nullable = false, unique = true)
    private String              username;
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String              password;
    private String              email;
    private String              oneTimePassword;
    private Boolean             admin       = false;
    private Boolean             locked      = false;
    @Convert(converter = HashMapConverter.class)
    private Map<String, String> metaData    = new HashMap<>();
    @ManyToMany(targetEntity = Authority.class, fetch = FetchType.EAGER)
    private List<Authority>     authorities = new ArrayList<>();


    public void setPassword(String password) {
        this.password = Md5Crypt.md5Crypt(password.getBytes());
    }

    public boolean checkPassword(String plainPassword) {
        ValidatorChain<String> validator = new ValidatorChain<>();
        validator.addValidator(new Md5PasswordValidator(this));

        return validator.isValid(plainPassword);
    }
}