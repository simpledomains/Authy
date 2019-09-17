package de.reynok.authentication.core.database.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.reynok.authentication.core.util.validation.Md5PasswordValidator;
import de.reynok.authentication.core.util.validation.OneTimePasswordValidator;
import de.reynok.authentication.core.util.validation.Validator;
import de.reynok.authentication.core.util.validation.ValidatorChain;
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
@ToString(exclude = {"password", "otpSecret"})
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
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String              otpSecret;
    private Boolean             admin       = false;
    private Boolean             locked      = false;
    @Convert(converter = HashMapConverter.class)
    private Map<String, String> metaData    = new HashMap<>();
    @ManyToMany(targetEntity = Authority.class, fetch = FetchType.EAGER)
    private List<Authority>     authorities = new ArrayList<>();


    public void setPassword(String password) {
        this.password = Md5Crypt.md5Crypt(password.getBytes());
    }

    public boolean checkPassword(String plainPassword, String securityPassword) {
        ValidatorChain<String> validator = new ValidatorChain<>();
        validator.addValidator(new Md5PasswordValidator(this));

        Validator<String> securityValidator = new OneTimePasswordValidator(this.otpSecret);

        return validator.isValid(plainPassword) && securityValidator.isValid(securityPassword);
    }
}