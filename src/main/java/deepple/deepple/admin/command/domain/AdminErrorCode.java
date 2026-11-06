package deepple.deepple.admin.command.domain;

import deepple.deepple.common.exception.ErrorCode;
import deepple.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminErrorCode implements ErrorCode {
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
    SCREENING_NOT_FOUND(ErrorType.NOT_FOUND, "심사를 찾을 수 없습니다."),
    CANNOT_REJECT_APPROVED_SCREENING(ErrorType.FAILED_PRECONDITION, "이미 승인된 심사는 반려할 수 없습니다."),
    INVALID_REJECTION_REASON(ErrorType.INVALID_ARGUMENT, "유효하지 않은 반려 사유입니다."),
    INVALID_SUSPENSION_STATUS(ErrorType.INVALID_ARGUMENT, "유효하지 않은 정지 상태입니다."),
    INVALID_WARNING_REASON_TYPE(ErrorType.INVALID_ARGUMENT, "유효하지 않은 경고 사유입니다.");

    private final ErrorType type;
    private final String message;
}
