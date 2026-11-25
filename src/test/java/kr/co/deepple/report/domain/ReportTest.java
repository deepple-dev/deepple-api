package kr.co.deepple.report.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.report.fixture.ReportFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReportTest {

  @Nested
  @DisplayName("of 메서드 테스트")
  class OfTest {

    @Test
    @DisplayName("reporterId와 reporteeId가 같으면 예외를 던진다.")
    void throwsExceptionWhenReporterIdAndReporteeIdAreEqual() {
      // given
      long reporterId = 1L;
      long reporteeId = 1L;
      ReportReasonType reason = ReportReasonType.ETC;
      String content = "content";

      // when, then
      assertThatThrownBy(() ->
              Report.of(
                  reporterId, reporteeId, ReportTargetType.MEMBER, reporteeId, reason, content))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", ReportErrorCode.SELF_REPORT);
    }

    @Test
    @DisplayName("of 메서드의 필드 값이 정상적이면 객체를 생성한다.")
    void createsObjectWhenFieldValueIsValid() {
      // given
      long reporterId = 1L;
      long reporteeId = 2L;
      ReportReasonType reason = ReportReasonType.ETC;
      String content = "content";

      // when
      Report report =
          Report.of(reporterId, reporteeId, ReportTargetType.FEED, 100L, reason, content);

      // then
      assertThat(report).isNotNull();
      assertThat(report.getReporterId()).isEqualTo(reporterId);
      assertThat(report.getReporteeId()).isEqualTo(reporteeId);
      assertThat(report.getTargetType()).isEqualTo(ReportTargetType.FEED);
      assertThat(report.getTargetId()).isEqualTo(100L);
      assertThat(report.getReason()).isEqualTo(reason);
      assertThat(report.getContent()).isEqualTo(content);
      assertThat(report.getResult()).isEqualTo(ReportResult.PENDING);
    }

    @Test
    @DisplayName("회원 신고의 대상 ID가 피신고자 ID와 다르면 예외를 던진다.")
    void throwsExceptionWhenMemberTargetIdDiffersFromReporteeId() {
      // when, then
      assertThatThrownBy(
              () -> Report.of(1L, 2L, ReportTargetType.MEMBER, 3L, ReportReasonType.ETC, "content"))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", ReportErrorCode.INVALID_REPORT_TARGET);
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
  }

  @Nested
  @DisplayName("hasVersionConflict 메서드 테스트")
  class HasVersionConflictTests {

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
    @DisplayName("Pending 상태의 report로 warn 호출 시 결과가 WARNED로 변경된다.")
    void shouldChangeResultToWarned() {
      // given
      Long adminId = 1L;
      Report report = ReportFixture.builder().build();

      // when
      report.warn(adminId);

      // then
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
  }
}
