package kr.co.deepple.notification.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {
  NOTIFICATION_PREFERENCE_NOT_FOUND(ErrorType.NOT_FOUND, "알림 설정을 찾을 수 없습니다."),
  INVALID_NOTIFICATION_TYPE(ErrorType.INVALID_ARGUMENT, "지원하지 않는 알림 타입입니다."),
  NOTIFICATION_SEND_FAILED(ErrorType.UNAVAILABLE, "알림 전송에 실패했습니다.");

  private final ErrorType type;
  private final String message;
}
