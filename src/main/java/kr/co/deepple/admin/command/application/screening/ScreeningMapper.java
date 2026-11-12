package kr.co.deepple.admin.command.application.screening;

import kr.co.deepple.admin.command.domain.AdminErrorCode;
import kr.co.deepple.admin.command.domain.screening.RejectionReasonType;
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
