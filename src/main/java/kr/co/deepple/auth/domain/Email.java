package kr.co.deepple.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class Email {

  private static final String EMAIL_REGEX = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
  private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

  @Column(name = "email")
  private final String address;

  private Email(@NonNull String address) {
    if (!EMAIL_PATTERN.matcher(address).matches()) {
      throw new DeeppleException(AuthErrorCode.INVALID_EMAIL);
    }
    this.address = address;
  }

  public static Email from(String address) {
    return new Email(address);
  }
}
