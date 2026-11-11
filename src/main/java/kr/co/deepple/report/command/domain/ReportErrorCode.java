package kr.co.deepple.report.command.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportErrorCode implements ErrorCode {
    REPORT_NOT_FOUND(ErrorType.NOT_FOUND, "신고를 찾을 수 없습니다."),
    REPORT_ALREADY_EXISTS(ErrorType.ALREADY_EXISTS, "이미 신고한 회원입니다."),
    SELF_REPORT(ErrorType.INVALID_ARGUMENT, "자기 자신을 신고할 수 없습니다."),
    INVALID_REPORT_REASON_TYPE(ErrorType.INVALID_ARGUMENT, "유효하지 않은 신고 사유입니다."),
    INVALID_REPORT_RESULT(ErrorType.INVALID_ARGUMENT, "유효하지 않은 신고 처리 결과입니다."),
    REPORT_ALREADY_PROCESSED(ErrorType.FAILED_PRECONDITION, "이미 처리된 신고입니다.");

    private final ErrorType type;
    private final String message;
}
