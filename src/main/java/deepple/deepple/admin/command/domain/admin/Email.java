package deepple.deepple.admin.command.domain.admin;

import deepple.deepple.admin.command.domain.admin.exception.InvalidEmailException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.regex.Pattern;

import static lombok.AccessLevel.PROTECTED;

@Embeddable
@NoArgsConstructor(access = PROTECTED, force = true)
@Getter
@EqualsAndHashCode
public class Email {

    private static final String EMAIL_REGEX = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Column(name = "email", unique = true)
    private final String address;

    private Email(@NonNull String address) {
        if (!EMAIL_PATTERN.matcher(address).matches()) {
            throw new InvalidEmailException(address);
        }
        this.address = address;
    }

    public static Email from(String address) {
        return new Email(address);
    }
}