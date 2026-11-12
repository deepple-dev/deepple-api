package kr.co.deepple.report.command.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;

import kr.co.deepple.common.event.Events;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.report.command.domain.event.ReportCreatedEvent;
import kr.co.deepple.report.command.domain.event.ReportWarnedEvent;
import kr.co.deepple.report.fixture.ReportFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class ReportTest {

  private static MockedStatic<Events> eventsMockedStatic;

  @BeforeEach
  void setUp() {
    eventsMockedStatic = Mockito.mockStatic(Events.class);
    eventsMockedStatic.when(() -> Events.raise(Mockito.any())).thenAnswer(invocation -> null);
  }

  @AfterEach
  void tearDown() {
    eventsMockedStatic.close();
  }

  @Nested
  @DisplayName("of 메서드 테스트")
  class OfTest {

    @ParameterizedTest
    @ValueSource(strings = {"reporterId", "reporteeId", "reason", "content"})
    @DisplayName("of 메서드의 필드 값이 null이면 예외를 던진다.")
    void throwsExceptionWhenFieldValueIsNull(String fieldName) {
      // given
      Long reporterId = fieldName.equals("reporterId") ? null : 1L;
      Long reporteeId = fieldName.equals("reporteeId") ? null : 2L;
      ReportReasonType reason = fieldName.equals("reason") ? null : ReportReasonType.ETC;
      String content = fieldName.equals("content") ? null : "content";

      // when, then
      assertThatThrownBy(() -> Report.of(reporterId, reporteeId, reason, content))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("reporterId와 reporteeId가 같으면 예외를 던진다.")
    void throwsExceptionWhenReporterIdAndReporteeIdAreEqual() {
      // given
      Long reporterId = 1L;
      Long reporteeId = 1L;
      ReportReasonType reason = ReportReasonType.ETC;
      String content = "content";

      // when, then
      assertThatThrownBy(() -> Report.of(reporterId, reporteeId, reason, content))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", ReportErrorCode.SELF_REPORT);
    }

    @Test
    @DisplayName("of 메서드의 필드 값이 정상적이면 객체를 생성하고 이벤트를 발행한다.")
    void createsObjectAndRaiseEventWhenFieldValueIsValid() {
      // given
      Long reporterId = 1L;
      Long reporteeId = 2L;
      ReportReasonType reason = ReportReasonType.ETC;
      String content = "content";

      // when
      Report report = Report.of(reporterId, reporteeId, reason, content);

      // then
      eventsMockedStatic.verify(
          () ->
              Events.raise(
                  argThat(event ->
                      event instanceof ReportCreatedEvent
                          && ((ReportCreatedEvent) event).getReporterId() == reporterId
                          && ((ReportCreatedEvent) event).getReporteeId() == reporteeId)),
          times(1));

      assertThat(report).isNotNull();
      assertThat(report.getReporterId()).isEqualTo(reporterId);
      assertThat(report.getReporteeId()).isEqualTo(reporteeId);
      assertThat(report.getReason()).isEqualTo(reason);
      assertThat(report.getContent()).isEqualTo(content);
      assertThat(report.getResult()).isEqualTo(ReportResult.PENDING);
    }
  }

  @Nested
  @DisplayName("reject 메서드 테스트")
  class RejectTest {

    @Test
    @DisplayName("Pending 상태의 report로 reject 메서드를 호출하면 ReportResult가 REJECTED로 변경된다.")
    void changesReportResultToRejected() {
      // given
      Long adminId = 1L;
      Report report = ReportFixture.builder().build();

      // when
      report.reject(adminId);

      // then
      assertThat(report.getResult()).isEqualTo(ReportResult.REJECTED);
    }

    @Test
    @DisplayName("ReportResult가 PENDING이 아닌 경우 reject 메서드를 호출하면 예외가 발생한다.")
    void throwsExceptionWhenReportResultIsNotPending() {
      // given
      Long adminId = 1L;
      Report report =
          ReportFixture.builder().result(ReportResult.REJECTED).adminId(adminId).build();

      // when, then
      assertThatThrownBy(() -> report.reject(adminId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", ReportErrorCode.REPORT_ALREADY_PROCESSED);
    }

    @Test
    @DisplayName("adminId가 null으로 reject 메서드를 호출하면 예외가 발생한다.")
    void throwsExceptionWhenAdminIdIsNull() {
      // given
      Long adminId = null;
      Report report = ReportFixture.builder().build();

      // when, then
      assertThatThrownBy(() -> report.reject(adminId)).isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("hasVersionConflict 메서드 테스트")
  class HasVersionConflictTests {

    @Test
    @DisplayName("version이 null인 경우 hasVersionConflict가 NullPointerException을 던진다.")
    void throwsExceptionWhenVersionIsNull() {
      // given
      Report report = ReportFixture.builder().build();

      // when, then
      assertThatThrownBy(() -> report.hasVersionConflict(1L))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("같은 version을 입력하면 false를 반환한다.")
    void shouldReturnFalseWhenVersionMatches() {
      // given
      Report report = ReportFixture.builder().version(5L).build();

      // when
      boolean result = report.hasVersionConflict(5L);

      // then
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("다른 version을 입력하면 true를 반환한다.")
    void shouldReturnTrueWhenVersionDiffers() {
      // given
      Report report = ReportFixture.builder().version(5L).build();

      // when
      boolean result = report.hasVersionConflict(3L);

      // then
      assertThat(result).isTrue();
    }
  }

  @Nested
  @DisplayName("warn 메서드 테스트")
  class WarnTest {

    @Test
    @DisplayName("Pending 상태의 report로 warn 호출 시 결과가 WARNED로 변경되고 이벤트를 발행한다.")
    void shouldChangeResultToWarnedAndRaiseEvent() {
      // given
      Long adminId = 1L;
      Report report = ReportFixture.builder().build();

      // when
      report.warn(adminId);

      // then
      eventsMockedStatic.verify(
          () ->
              Events.raise(
                  argThat(event ->
                      event instanceof ReportWarnedEvent
                          && ((ReportWarnedEvent) event).getReporteeId() == report.getReporteeId()
                          && ((ReportWarnedEvent) event)
                              .getReportReason()
                              .equals(report.getReason().name()))),
          times(1));
      assertThat(report.getResult()).isEqualTo(ReportResult.WARNED);
    }

    @Test
    @DisplayName("ReportResult가 PENDING이 아닌 경우 warn 메서드를 호출하면 예외가 발생한다.")
    void throwsExceptionWhenReportResultIsNotPending() {
      // given
      Long adminId = 1L;
      Report report = ReportFixture.builder().result(ReportResult.WARNED).adminId(adminId).build();

      // when, then
      assertThatThrownBy(() -> report.warn(adminId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", ReportErrorCode.REPORT_ALREADY_PROCESSED);
    }

    @Test
    @DisplayName("adminId가 null인 경우 warn 호출 시 예외가 발생한다.")
    void throwsExceptionWhenAdminIdIsNull() {
      // given
      Long adminId = null;
      Report report = ReportFixture.builder().build();

      // when, then
      assertThatThrownBy(() -> report.warn(adminId)).isInstanceOf(NullPointerException.class);
    }
  }
}
