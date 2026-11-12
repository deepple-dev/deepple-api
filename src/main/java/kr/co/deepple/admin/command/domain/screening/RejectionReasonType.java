package kr.co.deepple.admin.command.domain.screening;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RejectionReasonType {
  STOLEN_IMAGE("사진 도용"),
  INAPPROPRIATE_IMAGE("부적절한 사진"),
  EXPLICIT_CONTENT("과도한 성적 표현"),
  OFFENSIVE_LANGUAGE("욕설 및 불쾌감을 주는 표현"),
  CONTACT_IN_PROFILE("프로필 내 연락처 기재");

  private final String description;
}
