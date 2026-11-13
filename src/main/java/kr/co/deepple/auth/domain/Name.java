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
public class Name {

  private static final String NAME_REGEX = "^[a-zA-Z0-9가-\uD7AF]{1,10}$";
  private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);

  @Column(name = "name")
  private final String value;

  private Name(@NonNull String value) {
    if (!NAME_PATTERN.matcher(value).matches()) {
      throw new DeeppleException(AuthErrorCode.INVALID_NAME);
    }
    this.value = value;
  }

  public static Name from(String value) {
    return new Name(value);
  }
}
