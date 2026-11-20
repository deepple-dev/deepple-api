package kr.co.deepple.image.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageOwnerType {
  MEMBER("members"),
  ADMIN("admins");

  private final String pathSegment;
}
