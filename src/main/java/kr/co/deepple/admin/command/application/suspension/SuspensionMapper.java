package kr.co.deepple.admin.command.application.suspension;

import static lombok.AccessLevel.PRIVATE;

import kr.co.deepple.admin.command.domain.AdminErrorCode;
import kr.co.deepple.admin.command.domain.suspension.SuspensionStatus;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class SuspensionMapper {

  public static SuspensionStatus toSuspensionStatus(String status) {
    try {
      return SuspensionStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new DeeppleException(AdminErrorCode.INVALID_SUSPENSION_STATUS, "value=" + status, e);
    }
  }
}
