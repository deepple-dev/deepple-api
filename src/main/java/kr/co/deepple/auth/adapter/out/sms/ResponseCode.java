package kr.co.deepple.auth.adapter.out.sms;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {
  SUCCESS(200),
  EXPIRED_TOKEN(403);

  private final int code;
}
