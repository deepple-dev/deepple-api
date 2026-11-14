package kr.co.deepple.payment.domain;

import kr.co.deepple.common.exception.ErrorCode;
import kr.co.deepple.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
  ORDER_NOT_FOUND(ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다."),
  ORDER_ALREADY_EXISTS(ErrorType.ALREADY_EXISTS, "이미 처리된 주문입니다."),
  ORDER_NOT_REFUNDABLE(ErrorType.FAILED_PRECONDITION, "환불할 수 없는 주문입니다."),
  REVOKED_TRANSACTION(ErrorType.FAILED_PRECONDITION, "환불되거나 취소된 결제입니다."),
  HEART_PURCHASE_OPTION_NOT_FOUND(ErrorType.NOT_FOUND, "하트 구매 옵션을 찾을 수 없습니다."),
  HEART_PURCHASE_OPTION_ALREADY_EXISTS(ErrorType.ALREADY_EXISTS, "이미 존재하는 하트 구매 옵션입니다."),
  INVALID_HEART_PURCHASE_OPTION(ErrorType.INVALID_ARGUMENT, "유효하지 않은 하트 구매 옵션입니다."),
  INVALID_APP_STORE_RECEIPT(ErrorType.INVALID_ARGUMENT, "유효하지 않은 App Store 영수증입니다."),
  APP_STORE_UNAVAILABLE(ErrorType.UNAVAILABLE, "App Store 요청에 실패했습니다."),
  INVALID_GOOGLE_PLAY_RECEIPT(ErrorType.INVALID_ARGUMENT, "유효하지 않은 Google Play 영수증입니다."),
  GOOGLE_PLAY_UNAVAILABLE(ErrorType.UNAVAILABLE, "Google Play 요청에 실패했습니다.");

  private final ErrorType type;
  private final String message;
}
