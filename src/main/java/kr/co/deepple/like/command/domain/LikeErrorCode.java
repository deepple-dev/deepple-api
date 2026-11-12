package kr.co.deepple.like.command.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LikeErrorCode implements ErrorCode {
  LIKE_ALREADY_EXISTS(ErrorType.ALREADY_EXISTS, "이미 좋아요를 보낸 회원입니다."),
  LIKE_RECEIVER_INACTIVE(ErrorType.FAILED_PRECONDITION, "활동 중이 아닌 회원에게는 좋아요를 보낼 수 없습니다."),
  LIKE_SAME_GENDER(ErrorType.FAILED_PRECONDITION, "같은 성별에게는 좋아요를 보낼 수 없습니다."),
  LIKE_BLOCKED(ErrorType.PERMISSION_DENIED, "차단 관계인 회원에게는 좋아요를 보낼 수 없습니다.");

  private final ErrorType type;
  private final String message;
}
