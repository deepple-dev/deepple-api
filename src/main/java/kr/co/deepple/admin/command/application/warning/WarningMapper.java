package kr.co.deepple.admin.command.application.warning;

import static lombok.AccessLevel.PRIVATE;

import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.admin.command.domain.AdminErrorCode;
import kr.co.deepple.admin.command.domain.warning.WarningReasonType;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
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
