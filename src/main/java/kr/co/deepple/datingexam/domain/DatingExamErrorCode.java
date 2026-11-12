package kr.co.deepple.datingexam.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DatingExamErrorCode implements ErrorCode {
    DATING_EXAM_SUBJECT_NOT_FOUND(ErrorType.NOT_FOUND, "연애고사 과목을 찾을 수 없습니다."),
    DATING_EXAM_RESULT_NOT_FOUND(ErrorType.NOT_FOUND, "연애고사 제출 결과가 없습니다."),
    DATING_EXAM_ALREADY_SUBMITTED(ErrorType.ALREADY_EXISTS, "이미 제출한 연애고사 과목입니다."),
    INVALID_DATING_EXAM_SUBMIT(ErrorType.INVALID_ARGUMENT, "유효하지 않은 연애고사 답안입니다."),
    GENDER_NOT_SET(ErrorType.FAILED_PRECONDITION, "성별 설정이 필요합니다.");

    private final ErrorType type;
    private final String message;
}
