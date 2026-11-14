package kr.co.deepple.admin.application;

import kr.co.deepple.admin.domain.AdminErrorCode;
import kr.co.deepple.admin.domain.RejectionReasonType;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScreeningMapper {

  public static RejectionReasonType toRejectionReasonType(String rejectionReason) {
    try {
      return RejectionReasonType.valueOf(rejectionReason.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new DeeppleException(
          AdminErrorCode.INVALID_REJECTION_REASON, "value=" + rejectionReason, e);
    }
  }
}
