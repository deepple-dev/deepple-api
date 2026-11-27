package kr.co.deepple.conversation.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConversationErrorCode implements ErrorCode {
  CONVERSATION_NOT_FOUND(ErrorType.NOT_FOUND, "대화를 찾을 수 없습니다."),
  INVALID_CONVERSATION_CURSOR(ErrorType.INVALID_ARGUMENT, "유효하지 않은 대화 목록 커서입니다.");

  private final ErrorType type;
  private final String message;
}
