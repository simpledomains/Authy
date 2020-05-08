package io.virtuellewolke.authentication.core.database.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.reynok.authentication.core.shared.util.HashMapConverter;
import de.reynok.authentication.core.shared.util.PartialUpdateableModel;
import de.reynok.authentication.core.shared.util.validation.Md5PasswordValidator;
import de.reynok.authentication.core.shared.util.validation.StringEqualsValidator;
import de.reynok.authentication.core.shared.util.validation.ValidatorChain;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.codec.digest.Md5Crypt;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.*;

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
    private byte[]              avatar;
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

    public Boolean getOtpEnabled() {
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
        String[] forbiddenFields = new String[]{"username", "authorities", "admin", "locked", "apiToken"};

        return Arrays.stream(forbiddenFields).noneMatch(s -> Objects.equals(s, field.getName().toLowerCase()));
    }
}