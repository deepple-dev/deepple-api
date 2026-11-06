package deepple.deepple.member.command.domain.member.vo;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.member.command.domain.MemberErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.regex.Pattern;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@EqualsAndHashCode
public class PhoneNumber {
    private static final String PHONE_NUMBER_REGEX = "^010\\d{8}$";
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile(PHONE_NUMBER_REGEX);

    @Column(name = "phone_number", unique = true)
    private final String value;

    private PhoneNumber(@NonNull String value) {
        if (!PHONE_NUMBER_PATTERN.matcher(value).matches()) {
            throw new DeeppleException(MemberErrorCode.INVALID_PHONE_NUMBER);
        }
        this.value = value;
    }

    public static PhoneNumber from(String value) {
        return new PhoneNumber(value);
    }
}
