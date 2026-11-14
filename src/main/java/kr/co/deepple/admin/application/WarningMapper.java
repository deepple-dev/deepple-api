package kr.co.deepple.admin.application;

import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.admin.domain.AdminErrorCode;
import kr.co.deepple.admin.domain.WarningReasonType;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WarningMapper {

  public static Set<WarningReasonType> toWarningReasonTypes(Set<String> reasonTypes) {
    return reasonTypes.stream().map(WarningMapper::toWarningReasonType).collect(Collectors.toSet());
  }

  private static WarningReasonType toWarningReasonType(String reasonType) {
    try {
      return WarningReasonType.valueOf(reasonType.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new DeeppleException(
          AdminErrorCode.INVALID_WARNING_REASON_TYPE, "value=" + reasonType, e);
    }
  }
}
