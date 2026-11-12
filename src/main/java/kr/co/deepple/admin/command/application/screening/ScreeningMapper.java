package kr.co.deepple.admin.command.application.screening;

import static lombok.AccessLevel.PRIVATE;

import kr.co.deepple.admin.command.domain.AdminErrorCode;
import kr.co.deepple.admin.command.domain.screening.RejectionReasonType;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
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
