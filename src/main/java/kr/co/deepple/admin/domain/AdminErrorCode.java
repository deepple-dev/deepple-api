package kr.co.deepple.admin.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminErrorCode implements ErrorCode {
  SCREENING_NOT_FOUND(ErrorType.NOT_FOUND, "심사를 찾을 수 없습니다."),
  CANNOT_REJECT_APPROVED_SCREENING(ErrorType.FAILED_PRECONDITION, "이미 승인된 심사는 반려할 수 없습니다."),
  INVALID_REJECTION_REASON(ErrorType.INVALID_ARGUMENT, "유효하지 않은 반려 사유입니다."),
  INVALID_SUSPENSION_STATUS(ErrorType.INVALID_ARGUMENT, "유효하지 않은 정지 상태입니다."),
  INVALID_WARNING_REASON_TYPE(ErrorType.INVALID_ARGUMENT, "유효하지 않은 경고 사유입니다.");

  private final ErrorType type;
  private final String message;
}
