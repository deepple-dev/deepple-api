package kr.co.deepple.report.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import kr.co.deepple.member.application.port.in.MemberSummary;
import kr.co.deepple.report.adapter.out.persistence.ReportQueryRepository;
import kr.co.deepple.report.application.dto.RawReportDetailView;
import kr.co.deepple.report.application.dto.RawReportView;
import kr.co.deepple.report.application.dto.ReportDetailView;
import kr.co.deepple.report.application.dto.ReportSearchCondition;
import kr.co.deepple.report.application.dto.ReportView;
import kr.co.deepple.report.domain.ReportErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ReportQueryServiceTest {

  private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 11, 14, 0, 0);

  @Mock private ReportQueryRepository reportQueryRepository;

  @Mock private FindMemberSummaryUseCase findMemberSummaryUseCase;

  @InjectMocks private ReportQueryService reportQueryService;

  @Test
  @DisplayName("신고 목록에 신고자와 대상의 닉네임을 붙이고, 회원을 찾을 수 없으면 닉네임을 비워 둔다.")
  void attachNicknamesToReports() {
    ReportSearchCondition condition = new ReportSearchCondition(null, null, null);
    Pageable pageable = PageRequest.of(0, 10);
    when(reportQueryRepository.getPage(condition, pageable))
        .thenReturn(
            new PageImpl<>(
                List.of(new RawReportView(1L, 10L, 20L, "ETC", "PENDING", CREATED_AT)),
                pageable,
                1));
    when(findMemberSummaryUseCase.findMemberSummaries(Set.of(10L, 20L)))
        .thenReturn(Map.of(10L, summary(10L, "신고자")));

    Page<ReportView> page = reportQueryService.getPage(condition, pageable);

    assertThat(page.getTotalElements()).isEqualTo(1);
    ReportView view = page.getContent().getFirst();
    assertThat(view.id()).isEqualTo(1L);
    assertThat(view.reporterId()).isEqualTo(10L);
    assertThat(view.reporterNickname()).isEqualTo("신고자");
    assertThat(view.reporteeId()).isEqualTo(20L);
    assertThat(view.reporteeNickname()).isNull();
    assertThat(view.reason()).isEqualTo("ETC");
    assertThat(view.result()).isEqualTo("PENDING");
    assertThat(view.createdAt()).isEqualTo(CREATED_AT);
  }

  @Test
  @DisplayName("신고 상세에 신고자와 대상의 닉네임을 붙인다.")
  void attachNicknamesToReportDetail() {
    when(reportQueryRepository.findReportDetailView(1L))
        .thenReturn(
            Optional.of(
                new RawReportDetailView(1L, 3L, 10L, 20L, "ETC", "PENDING", "내용", CREATED_AT)));
    when(findMemberSummaryUseCase.findMemberSummaries(Set.of(10L, 20L)))
        .thenReturn(Map.of(10L, summary(10L, "신고자"), 20L, summary(20L, "대상")));

    ReportDetailView view = reportQueryService.getReport(1L);

    assertThat(view.version()).isEqualTo(3L);
    assertThat(view.reporterNickname()).isEqualTo("신고자");
    assertThat(view.reporteeNickname()).isEqualTo("대상");
    assertThat(view.content()).isEqualTo("내용");
  }

  @Test
  @DisplayName("없는 신고를 조회하면 신고를 찾을 수 없다는 예외를 던진다.")
  void throwWhenReportNotFound() {
    when(reportQueryRepository.findReportDetailView(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> reportQueryService.getReport(1L))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", ReportErrorCode.REPORT_NOT_FOUND);
  }

  private MemberSummary summary(long memberId, String nickname) {
    return new MemberSummary(
        memberId, nickname, "MALE", 30, "SEOUL", null, "SILVER", "ACTIVE", false);
  }
}
