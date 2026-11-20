package kr.co.deepple.image.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageErrorCode implements ErrorCode {
  UNSUPPORTED_IMAGE_CONTENT_TYPE(ErrorType.INVALID_ARGUMENT, "JPEG, PNG, WebP 이미지만 올릴 수 있습니다."),
  IMAGE_NOT_FOUND(ErrorType.NOT_FOUND, "이미지를 찾을 수 없습니다."),
  IMAGE_PROCESSING_FAILED(ErrorType.FAILED_PRECONDITION, "처리에 실패한 이미지입니다.");

  private final ErrorType type;
  private final String message;
}
