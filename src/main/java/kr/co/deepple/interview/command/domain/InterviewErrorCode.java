package kr.co.deepple.interview.command.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InterviewErrorCode implements ErrorCode {
  INTERVIEW_QUESTION_NOT_FOUND(ErrorType.NOT_FOUND, "인터뷰 질문을 찾을 수 없습니다."),
  INTERVIEW_QUESTION_ALREADY_EXISTS(ErrorType.ALREADY_EXISTS, "이미 존재하는 인터뷰 질문입니다."),
  INTERVIEW_QUESTION_NOT_PUBLIC(ErrorType.FAILED_PRECONDITION, "공개되지 않은 인터뷰 질문입니다."),
  INVALID_INTERVIEW_QUESTION_CONTENT(ErrorType.INVALID_ARGUMENT, "인터뷰 질문 내용을 입력해 주세요."),
  INVALID_INTERVIEW_CATEGORY(ErrorType.INVALID_ARGUMENT, "유효하지 않은 인터뷰 카테고리입니다."),
  INTERVIEW_ANSWER_NOT_FOUND(ErrorType.NOT_FOUND, "인터뷰 답변을 찾을 수 없습니다."),
  INTERVIEW_ANSWER_ALREADY_EXISTS(ErrorType.ALREADY_EXISTS, "이미 답변한 인터뷰 질문입니다."),
  INTERVIEW_ANSWER_ACCESS_DENIED(ErrorType.PERMISSION_DENIED, "인터뷰 답변에 접근할 권한이 없습니다."),
  INVALID_INTERVIEW_ANSWER_CONTENT(ErrorType.INVALID_ARGUMENT, "인터뷰 답변 내용을 입력해 주세요.");

  private final ErrorType type;
  private final String message;
}
