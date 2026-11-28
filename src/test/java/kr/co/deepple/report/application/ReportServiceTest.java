package kr.co.deepple.report.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.report.application.dto.ReportCreateRequest;
import kr.co.deepple.report.application.port.out.ReportRepository;
import kr.co.deepple.report.application.port.out.spi.ReportTargetFinder;
import kr.co.deepple.report.domain.ContentReportedEvent;
import kr.co.deepple.report.domain.ReportErrorCode;
import kr.co.deepple.report.domain.ReportReasonType;
import kr.co.deepple.report.domain.ReportTargetType;
import kr.co.deepple.report.event.ReportCreatedEvent;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.hibernate.exception.DataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

  private static final long REPORTER_ID = 2L;
  private static final long REPORTEE_ID = 1L;
  private static final long FEED_ID = 100L;
  private static final long COMMENT_ID = 200L;

  @Mock private ApplicationEventPublisher eventPublisher;

  @Mock private ReportRepository reportRepository;

  @Mock private FindMemberSummaryUseCase findMemberSummaryUseCase;

  @Mock private ReportTargetFinder reportTargetFinder;

  @InjectMocks private ReportService reportService;

  @Test
  @DisplayName("reportee가 존재하지 않으면 예외를 던진다.")
  void throwsExceptionWhenReporteeDoesNotExist() {
    // given
    when(findMemberSummaryUseCase.getMemberSummary(REPORTEE_ID))
        .thenThrow(new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND));

    // when, then
    assertThatThrownBy(() -> reportService.report(memberRequest(), REPORTER_ID))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);

    verify(reportRepository, never()).save(any());
  }

  @Test
  @DisplayName("같은 대상을 이미 신고했으면 대상을 확인하지 않고 예외를 던진다.")
  void throwsExceptionWhenReportAlreadyExists() {
    // given
    when(reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
            REPORTER_ID, ReportTargetType.FEED, FEED_ID))
        .thenReturn(true);

    // when, then
    assertThatThrownBy(() -> reportService.report(feedRequest(REPORTEE_ID), REPORTER_ID))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", ReportErrorCode.REPORT_ALREADY_EXISTS);

    verifyNoInteractions(reportTargetFinder);
    verify(reportRepository, never()).save(any());
  }

  @Test
  @DisplayName("동시에 같은 대상을 신고해 unique 제약에 걸리면 이미 신고했다는 예외를 던진다.")
  void throwsExceptionWhenUniqueConstraintIsViolated() {
    // given
    when(reportTargetFinder.findFeedAuthorMemberId(REPORTER_ID, FEED_ID))
        .thenReturn(Optional.of(REPORTEE_ID));
    when(reportRepository.save(any()))
        .thenThrow(
            new DataIntegrityViolationException(
                "중복",
                new ConstraintViolationException(
                    "중복",
                    new SQLException(),
                    ConstraintKind.UNIQUE,
                    "reports.uk_reports_target_type_target_id_reporter_id")));

    // when, then
    assertThatThrownBy(() -> reportService.report(feedRequest(REPORTEE_ID), REPORTER_ID))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", ReportErrorCode.REPORT_ALREADY_EXISTS);

    verify(eventPublisher, never()).publishEvent(any(Object.class));
  }

  @Test
  @DisplayName("중복 신고가 아닌 무결성 위반은 이미 신고했다는 예외로 바꾸지 않고 그대로 던진다.")
  void rethrowsIntegrityViolationOtherThanDuplicateReport() {
    // given
    when(reportTargetFinder.findFeedAuthorMemberId(REPORTER_ID, FEED_ID))
        .thenReturn(Optional.of(REPORTEE_ID));
    DataIntegrityViolationException tooLong =
        new DataIntegrityViolationException(
            "길이 초과", new DataException("길이 초과", new SQLException()));
    DataIntegrityViolationException notNull =
        new DataIntegrityViolationException(
            "null",
            new ConstraintViolationException(
                "null", new SQLException(), ConstraintKind.NOT_NULL, "target_type"));
    DataIntegrityViolationException otherUniqueKey =
        new DataIntegrityViolationException(
            "중복",
            new ConstraintViolationException(
                "중복", new SQLException(), ConstraintKind.UNIQUE, "reports.PRIMARY"));
    DataIntegrityViolationException noCause = new DataIntegrityViolationException("원인 없음");
    List<DataIntegrityViolationException> violations =
        List.of(tooLong, notNull, otherUniqueKey, noCause);
    when(reportRepository.save(any())).thenThrow(violations.toArray(RuntimeException[]::new));

    // when, then
    for (DataIntegrityViolationException violation : violations) {
      assertThatThrownBy(() -> reportService.report(feedRequest(REPORTEE_ID), REPORTER_ID))
          .isSameAs(violation);
    }

    verify(eventPublisher, never()).publishEvent(any(Object.class));
  }

  @Test
  @DisplayName("reason이 틀린 값이면 예외를 던진다.")
  void throwsExceptionWhenReasonIsInvalid() {
    // given
    ReportCreateRequest request =
        new ReportCreateRequest(REPORTEE_ID, "MEMBER", REPORTEE_ID, "INVALID_REASON", "content");

    // when, then
    assertThatThrownBy(() -> reportService.report(request, REPORTER_ID))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", ReportErrorCode.INVALID_REPORT_REASON_TYPE);

    verify(reportRepository, never()).save(any());
  }

  @Test
  @DisplayName("신고 대상 종류가 틀린 값이면 예외를 던진다.")
  void throwsExceptionWhenTargetTypeIsInvalid() {
    // given
    ReportCreateRequest request =
        new ReportCreateRequest(REPORTEE_ID, "PROFILE", REPORTEE_ID, "ETC", "content");

    // when, then
    assertThatThrownBy(() -> reportService.report(request, REPORTER_ID))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", ReportErrorCode.INVALID_REPORT_TARGET_TYPE);

    verify(reportRepository, never()).save(any());
  }

  @Test
  @DisplayName("피드 작성자가 reporteeId와 다르거나 공식 계정이면 예외를 던진다.")
  void throwsExceptionWhenFeedAuthorDoesNotMatch() {
    // given
    when(reportTargetFinder.findFeedAuthorMemberId(REPORTER_ID, FEED_ID))
        .thenReturn(Optional.of(3L), Optional.empty());

    // when, then
    for (int i = 0; i < 2; i++) {
      assertThatThrownBy(() -> reportService.report(feedRequest(REPORTEE_ID), REPORTER_ID))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", ReportErrorCode.INVALID_REPORT_TARGET);
    }

    verify(reportRepository, never()).save(any());
  }

  @Test
  @DisplayName("회원을 신고하면 저장하고 자동 차단 이벤트만 발행한다.")
  void savesMemberReport() {
    // when
    reportService.report(memberRequest(), REPORTER_ID);

    // then
    verify(reportRepository)
        .save(
            argThat(report ->
                report.getReporterId() == REPORTER_ID
                    && report.getReporteeId() == REPORTEE_ID
                    && report.getTargetType() == ReportTargetType.MEMBER
                    && report.getTargetId() == REPORTEE_ID
                    && report.getReason() == ReportReasonType.ETC));
    verify(eventPublisher).publishEvent(any(ReportCreatedEvent.class));
    verify(eventPublisher, never()).publishEvent(any(ContentReportedEvent.class));
  }

  @Test
  @DisplayName("댓글을 신고하면 저장하고 자동 차단 이벤트와 누적 확인 이벤트를 발행한다.")
  void savesCommentReport() {
    // given
    when(reportTargetFinder.findCommentAuthorMemberId(REPORTER_ID, COMMENT_ID))
        .thenReturn(Optional.of(REPORTEE_ID));
    ReportCreateRequest request =
        new ReportCreateRequest(REPORTEE_ID, "COMMENT", COMMENT_ID, "HATE_OR_HARASSMENT", "");

    // when
    reportService.report(request, REPORTER_ID);

    // then
    verify(reportRepository)
        .save(
            argThat(report ->
                report.getTargetType() == ReportTargetType.COMMENT
                    && report.getTargetId() == COMMENT_ID));
    verify(eventPublisher).publishEvent(any(ReportCreatedEvent.class));
    verify(eventPublisher)
        .publishEvent(new ContentReportedEvent(ReportTargetType.COMMENT, COMMENT_ID));
  }

  private static ReportCreateRequest memberRequest() {
    return new ReportCreateRequest(REPORTEE_ID, "MEMBER", REPORTEE_ID, "ETC", "content");
  }

  private static ReportCreateRequest feedRequest(long reporteeId) {
    return new ReportCreateRequest(reporteeId, "FEED", FEED_ID, "SPAM_OR_ADVERTISING", "content");
  }
}
