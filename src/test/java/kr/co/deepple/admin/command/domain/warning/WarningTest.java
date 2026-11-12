package kr.co.deepple.admin.command.domain.warning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import java.util.Set;
import kr.co.deepple.common.event.Events;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class WarningTest {

  @Test
  @DisplayName("issue() 호출 시 WarningIssuedEvent 발생 및 Warning 객체가 올바르게 반환된다")
  void issueShouldRaiseWarningIssuedEventAndReturnWarning() {
    try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {
      // given
      long adminId = 1L;
      long memberId = 2L;
      long warningCount = 1;
      var reasons = Set.of(WarningReasonType.INAPPROPRIATE_INTERVIEW);

      // when
      var warning = Warning.issue(adminId, memberId, warningCount, reasons, true);

      // then
      eventsMock.verify(() -> Events.raise(any(WarningIssuedEvent.class)));
      assertEquals(adminId, warning.getAdminId());
      assertEquals(memberId, warning.getMemberId());
      assertEquals(reasons, warning.getReasonTypes());
    }
  }
}
