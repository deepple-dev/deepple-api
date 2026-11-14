package kr.co.deepple.admin.application;

import kr.co.deepple.admin.domain.AdminErrorCode;
import kr.co.deepple.admin.domain.SuspensionStatus;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SuspensionMapper {

  public static SuspensionStatus toSuspensionStatus(String status) {
    try {
      return SuspensionStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new DeeppleException(AdminErrorCode.INVALID_SUSPENSION_STATUS, "value=" + status, e);
    }
  }
}
