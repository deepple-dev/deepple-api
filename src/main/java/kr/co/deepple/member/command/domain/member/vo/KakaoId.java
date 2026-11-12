package kr.co.deepple.member.command.domain.member.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@EqualsAndHashCode
public class KakaoId {

  private static final String KAKAO_ID_REGEX = "^[a-zA-Z0-9._-]{3,15}$";

  @Column(name = "kakaoId")
  private String value;

  private KakaoId(String value) {
    if (!value.matches(KAKAO_ID_REGEX)) {
      throw new DeeppleException(MemberErrorCode.INVALID_KAKAO_ID);
    }
    this.value = value;
  }

  public static KakaoId from(String kakaoId) {
    return new KakaoId(kakaoId);
  }
}
