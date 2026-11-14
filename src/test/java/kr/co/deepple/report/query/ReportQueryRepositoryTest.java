package kr.co.deepple.report.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.report.command.domain.Report;
import kr.co.deepple.report.command.domain.ReportReasonType;
import kr.co.deepple.report.command.domain.ReportResult;
import kr.co.deepple.report.fixture.ReportFixture;
import kr.co.deepple.report.query.condition.ReportSearchCondition;
import kr.co.deepple.report.query.view.ReportDetailView;
import kr.co.deepple.report.query.view.ReportView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class ReportQueryRepositoryTest extends RepositoryTestSupport {

  @Autowired private TestEntityManager entityManager;

  @Autowired private ReportQueryRepository reportQueryRepository;

  private Member createMember(String phoneNumber, String nickname) {
    Member member = MemberFixture.builder().phoneNumber(phoneNumber).nickname(nickname).build();
    entityManager.persist(member);
    return member;
  }

  private Report createReport(
      Member reporter, Member reportee, ReportReasonType reason, ReportResult result) {
    Report report =
        ReportFixture.builder()
            .reporterId(reporter.getId())
            .reporteeId(reportee.getId())
            .reason(reason)
            .content("test content")
            .result(result)
            .adminId(result == ReportResult.PENDING ? null : 1L)
            .build();
    entityManager.persist(report);
    return report;
  }

  @Nested
  @DisplayName("getPage 메소드 테스트")
  class GetPageMethodTests {

    @Test
    @DisplayName("리포트 조회 시 모든 필드가 정확히 매핑되어 DTO 반환")
    void shouldReturnExactReportView() {
      // given
      Member reporter = createMember("01000000001", "Reporter1");
      Member reportee = createMember("01000000002", "Reportee1");
      Report report = createReport(reporter, reportee, ReportReasonType.ETC, ReportResult.PENDING);
      entityManager.flush();

      ReportSearchCondition condition = new ReportSearchCondition(null, null, null);

      // when
      Page<ReportView> page = reportQueryRepository.getPage(condition, PageRequest.of(0, 10));
      List<ReportView> content = page.getContent();

      // then
      assertThat(content).hasSize(1);
      ReportView view = content.get(0);
      assertThat(view.id()).isEqualTo(report.getId());
      assertThat(view.reporterId()).isEqualTo(reporter.getId());
      assertThat(view.reporterNickname()).isEqualTo(reporter.getProfile().getNickname().getValue());
      assertThat(view.reporteeId()).isEqualTo(reportee.getId());
      assertThat(view.reporteeNickname()).isEqualTo(reportee.getProfile().getNickname().getValue());
      assertThat(view.reason()).isEqualTo(report.getReason().name());
      assertThat(view.result()).isEqualTo(report.getResult().name());
      assertThat(view.createdAt()).isCloseTo(report.getCreatedAt(), within(1, ChronoUnit.MICROS));
    }

    @Test
    @DisplayName("결과가 PENDING인 경우만 반환")
    void shouldReturnRecordsWhenResultMatches() {
      // given
      Member reporter = createMember("01000000001", "Reporter1");
      Member reportee = createMember("01000000002", "Reportee1");
      createReport(reporter, reportee, ReportReasonType.INAPPROPRIATE_IMAGE, ReportResult.PENDING);
      createReport(reporter, reportee, ReportReasonType.INAPPROPRIATE_IMAGE, ReportResult.WARNED);
      entityManager.flush();

      ReportSearchCondition condition =
          new ReportSearchCondition(ReportResult.PENDING.name(), null, null);

      // when
      Page<ReportView> page = reportQueryRepository.getPage(condition, PageRequest.of(0, 10));

      // then
      assertThat(page.getTotalElements()).isEqualTo(1);
      assertThat(page.getContent())
          .extracting(ReportView::result)
          .allMatch(res -> res.equals(ReportResult.PENDING.name()));
    }

    @Test
    @DisplayName("result가 null이면 전체 반환")
    void shouldReturnAllWhenResultIsNull() {
      // given
      Member reporter = createMember("01000000001", "Reporter1");
      Member reportee = createMember("01000000002", "Reportee1");
      createReport(reporter, reportee, ReportReasonType.OFFENSIVE_LANGUAGE, ReportResult.REJECTED);
      createReport(reporter, reportee, ReportReasonType.OFFENSIVE_LANGUAGE, ReportResult.WARNED);
      entityManager.flush();

      ReportSearchCondition condition = new ReportSearchCondition(null, null, null);

      // when
      Page<ReportView> page = reportQueryRepository.getPage(condition, PageRequest.of(0, 10));

      // then
      assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("createdAt >= 지정 날짜 이후인 경우 반환")
    void shouldReturnRecordsWithCreatedAtAfterOrEqual() {
      // given
      Member reporter = createMember("01000000001", "Reporter1");
      Member reportee = createMember("01000000002", "Reportee1");
      Report report =
          createReport(
              reporter, reportee, ReportReasonType.CONTACT_IN_PROFILE, ReportResult.PENDING);
      entityManager.flush();

      LocalDate reportDate = report.getCreatedAt().toLocalDate();
      ReportSearchCondition condSame = new ReportSearchCondition(null, reportDate, null);
      ReportSearchCondition condNext =
          new ReportSearchCondition(null, reportDate.plusDays(1), null);

      // when
      Page<ReportView> pageSame = reportQueryRepository.getPage(condSame, PageRequest.of(0, 10));
      Page<ReportView> pageNext = reportQueryRepository.getPage(condNext, PageRequest.of(0, 10));

      // then
      assertThat(pageSame.getTotalElements()).isEqualTo(1);
      assertThat(pageNext.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("createdAt <= 지정 날짜 이전인 경우 반환")
    void shouldReturnRecordsWithCreatedAtBeforeOrEqual() {
      // given
      Member reporter = createMember("01000000001", "Reporter1");
      Member reportee = createMember("01000000002", "Reportee1");
      Report report =
          createReport(
              reporter, reportee, ReportReasonType.EXPLICIT_CONTENT, ReportResult.REJECTED);
      entityManager.flush();
      LocalDate reportDate = report.getCreatedAt().toLocalDate();

      ReportSearchCondition condSame = new ReportSearchCondition(null, null, reportDate);

      ReportSearchCondition condPrev =
          new ReportSearchCondition(null, null, reportDate.minusDays(1));

      // when
      Page<ReportView> pageSame = reportQueryRepository.getPage(condSame, PageRequest.of(0, 10));
      Page<ReportView> pagePrev = reportQueryRepository.getPage(condPrev, PageRequest.of(0, 10));

      // then
      assertThat(pageSame.getTotalElements()).isEqualTo(1);
      assertThat(pagePrev.getTotalElements()).isZero();
    }
  }

  @Nested
  @DisplayName("findReportDetailView 메소드 테스트")
  class FindReportDetailViewMethodTests {

    @Test
    @DisplayName("리포트 조회 시 모든 필드가 정확히 매핑되어 DTO 반환")
    void shouldReturnExactReportDetailView() {
      // given
      Member reporter = createMember("01000000001", "Reporter1");
      Member reportee = createMember("01000000002", "Reportee1");
      Report report = createReport(reporter, reportee, ReportReasonType.ETC, ReportResult.PENDING);
      entityManager.flush();

      // when
      Optional<ReportDetailView> optionalView =
          reportQueryRepository.findReportDetailView(report.getId());

      // then
      assertThat(optionalView).isPresent();

      ReportDetailView view = optionalView.get();
      assertThat(view.id()).isEqualTo(report.getId());
      assertThat(view.version()).isEqualTo(report.getVersion());
      assertThat(view.reporterId()).isEqualTo(reporter.getId());
      assertThat(view.reporterNickname()).isEqualTo(reporter.getProfile().getNickname().getValue());
      assertThat(view.reporteeId()).isEqualTo(reportee.getId());
      assertThat(view.reporteeNickname()).isEqualTo(reportee.getProfile().getNickname().getValue());
      assertThat(view.reason()).isEqualTo(report.getReason().name());
      assertThat(view.result()).isEqualTo(report.getResult().name());
      assertThat(view.content()).isEqualTo(report.getContent());
      assertThat(view.createdAt()).isCloseTo(report.getCreatedAt(), within(1, ChronoUnit.MICROS));
    }

    @Test
    @DisplayName("존재하지 않는 리포트 ID로 조회 시 Optional.empty() 반환")
    void shouldReturnEmptyOptionalWhenReportNotFound() {
      // given
      long nonExistentId = 999L;

      // when
      Optional<ReportDetailView> optionalView =
          reportQueryRepository.findReportDetailView(nonExistentId);

      // then
      assertThat(optionalView).isEmpty();
    }
  }
}
