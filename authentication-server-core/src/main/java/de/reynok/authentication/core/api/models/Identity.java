package de.reynok.authentication.core.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.reynok.authentication.core.util.HashMapConverter;
import de.reynok.authentication.core.util.validation.Md5PasswordValidator;
import de.reynok.authentication.core.util.validation.StringEqualsValidator;
import de.reynok.authentication.core.util.validation.ValidatorChain;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.codec.digest.Md5Crypt;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@ToString(exclude = {"password", "otpSecret", "apiToken"})
@Table(name = "identity")
@EqualsAndHashCode(exclude = {"authorities", "metaData"}, callSuper = false)
public class Identity extends PartialUpdateableModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer             id;
    @Column(nullable = false, unique = true)
    private String              username;
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String              password;
    private String              email;
    private String              displayName;
    private String              avatar;
    @Column(unique = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String              apiToken;
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

    public boolean getOtpEnabled() {
        return otpSecret != null;
    }

    public boolean checkPassword(String plainPassword) {
        ValidatorChain<String> validator = new ValidatorChain<>();
        validator.addValidator(new Md5PasswordValidator(this));
        validator.addValidator(new StringEqualsValidator(this.getPassword()));

        return validator.isValid(plainPassword);
    }

    @Override
    protected void onUpdateField(Field field, Object value) {
        if (field.getName().equals("password")) {
            this.setPassword(value.toString());
        } else {
            super.onUpdateField(field, value);
        }
    }

    @Override
    protected boolean canUpdateField(Field field) {
        return !field.getName().equals("username") &&
                !field.getName().equals("authorities") &&
                !field.getName().equals("admin") &&
                !field.getName().equals("locked") &&
                !field.getName().equals("apiToken");
    }
}