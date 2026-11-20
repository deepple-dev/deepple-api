package kr.co.deepple.block.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BlockErrorCode implements ErrorCode {
  SELF_BLOCK(ErrorType.INVALID_ARGUMENT, "자기 자신을 차단할 수 없습니다.");

  private final ErrorType type;
  private final String message;
}
