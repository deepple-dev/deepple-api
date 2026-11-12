package kr.co.deepple.admin.command.domain.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import kr.co.deepple.admin.command.domain.AdminErrorCode;
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
public class Password {

  private static final String PASSWORD_REGEX =
      "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{10,20}$";
  private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

  @Column(name = "password")
  private final String hashedValue;

  private Password(@NonNull String hashedValue) {
    if (hashedValue.isBlank()) {
      throw new IllegalArgumentException("비밀번호 해시가 비어 있습니다.");
    }
    this.hashedValue = hashedValue;
  }

  public static Password fromRaw(@NonNull String rawValue, PasswordHasher hasher) {
    if (!PASSWORD_PATTERN.matcher(rawValue).matches()) {
      throw new DeeppleException(AdminErrorCode.INVALID_PASSWORD);
    }
    String hashedValue = hasher.hash(rawValue);
    return new Password(hashedValue);
  }

  public static Password fromHashed(String hashedValue) {
    return new Password(hashedValue);
  }

  public boolean matches(String rawValue, PasswordHasher hasher) {
    return hasher.matches(rawValue, hashedValue);
  }
}
