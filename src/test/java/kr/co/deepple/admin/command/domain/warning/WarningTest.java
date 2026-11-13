package kr.co.deepple.admin.command.domain.warning;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WarningTest {

  @Test
  @DisplayName("issue() 호출 시 Warning 객체가 올바르게 반환된다")
  void issueShouldReturnWarning() {
    // given
    long adminId = 1L;
    long memberId = 2L;
    var reasons = Set.of(WarningReasonType.INAPPROPRIATE_INTERVIEW);

    // when
    var warning = Warning.issue(adminId, memberId, reasons, true, null);

    // then
    assertEquals(adminId, warning.getAdminId());
    assertEquals(memberId, warning.getMemberId());
    assertEquals(reasons, warning.getReasonTypes());
  }
}
