package kr.co.deepple.member.command.domain.member.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class Nickname {

  private static final String NICKNAME_REGEX = "^[a-zA-Z0-9가-힣]{1,10}$";

  @Column(name = "nickname")
  private final String value;

  private Nickname(@NonNull String value) {
    if (!value.matches(NICKNAME_REGEX)) {
      throw new DeeppleException(MemberErrorCode.INVALID_NICKNAME);
    }
    this.value = value;
  }

  public static Nickname from(String nickname) {
    return new Nickname(nickname);
  }
}
