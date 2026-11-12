package kr.co.deepple.admin.command.domain.admin;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import kr.co.deepple.admin.command.domain.AdminErrorCode;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Embeddable
@NoArgsConstructor(access = PROTECTED, force = true)
@Getter
@EqualsAndHashCode
public class Name {

  private static final String NAME_REGEX = "^[a-zA-Z0-9가-\uD7AF]{1,10}$";
  private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);

  @Column(name = "name")
  private final String value;

  private Name(@NonNull String value) {
    if (!NAME_PATTERN.matcher(value).matches()) {
      throw new DeeppleException(AdminErrorCode.INVALID_NAME);
    }
    this.value = value;
  }

  public static Name from(String value) {
    return new Name(value);
  }
}
