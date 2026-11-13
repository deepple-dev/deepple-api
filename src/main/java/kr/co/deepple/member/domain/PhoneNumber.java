package kr.co.deepple.member.domain;

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
public class PhoneNumber {

  private static final String PHONE_NUMBER_REGEX = "^010\\d{8}$";
  private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile(PHONE_NUMBER_REGEX);

  @Column(name = "phone_number")
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
