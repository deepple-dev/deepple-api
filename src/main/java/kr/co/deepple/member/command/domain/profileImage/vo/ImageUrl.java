package kr.co.deepple.member.command.domain.profileImage.vo;

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
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ImageUrl {

  @Column(name = "url")
  private final String value;

  private ImageUrl(String value) {
    if (value == null || value.isEmpty()) {
      throw new DeeppleException(MemberErrorCode.INVALID_PROFILE_IMAGE_URL);
    }
    this.value = value;
  }

  public static ImageUrl from(String value) {
    return new ImageUrl(value);
  }
}
