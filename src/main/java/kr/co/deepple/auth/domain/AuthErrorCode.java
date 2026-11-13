package kr.co.deepple.auth.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
  MISSING_REFRESH_TOKEN(ErrorType.UNAUTHENTICATED, "리프레시 토큰이 없습니다."),
  INVALID_REFRESH_TOKEN(ErrorType.UNAUTHENTICATED, "유효하지 않은 리프레시 토큰입니다."),
  INVALID_CREDENTIALS(ErrorType.UNAUTHENTICATED, "이메일 또는 비밀번호가 일치하지 않습니다."),
  ADMIN_NOT_APPROVED(ErrorType.PERMISSION_DENIED, "승인 대기 중인 관리자입니다."),
  ADMIN_EMAIL_ALREADY_EXISTS(ErrorType.ALREADY_EXISTS, "이미 사용 중인 이메일입니다."),
  PENDING_ADMIN_NOT_FOUND(ErrorType.NOT_FOUND, "승인 대기 중인 관리자를 찾을 수 없습니다."),
  ADMIN_NOT_PENDING(ErrorType.FAILED_PRECONDITION, "승인 대기 중인 관리자만 승인할 수 있습니다."),
  INVALID_ADMIN_ROLE(ErrorType.INVALID_ARGUMENT, "유효하지 않은 관리자 역할입니다."),
  INVALID_EMAIL(ErrorType.INVALID_ARGUMENT, "유효하지 않은 이메일 형식입니다."),
  INVALID_NAME(ErrorType.INVALID_ARGUMENT, "이름은 문자와 숫자로 10자 이내여야 합니다."),
  INVALID_PASSWORD(ErrorType.INVALID_ARGUMENT, "유효하지 않은 비밀번호 형식입니다."),
  INVALID_PHONE_NUMBER(ErrorType.INVALID_ARGUMENT, "유효하지 않은 전화번호 형식입니다."),
  MEMBER_DELETED(ErrorType.PERMISSION_DENIED, "탈퇴한 회원입니다."),
  MEMBER_PERMANENTLY_SUSPENDED(ErrorType.PERMISSION_DENIED, "영구 정지된 회원입니다."),
  MEMBER_TEMPORARILY_SUSPENDED(ErrorType.PERMISSION_DENIED, "일시 정지된 회원입니다."),
  AUTH_CODE_NOT_FOUND(ErrorType.NOT_FOUND, "인증번호가 만료되었거나 발송되지 않았습니다."),
  AUTH_CODE_MISMATCH(ErrorType.INVALID_ARGUMENT, "인증번호가 일치하지 않습니다."),
  SMS_SEND_FAILED(ErrorType.UNAVAILABLE, "인증번호 발송에 실패했습니다.");

  private final ErrorType type;
  private final String message;
}
