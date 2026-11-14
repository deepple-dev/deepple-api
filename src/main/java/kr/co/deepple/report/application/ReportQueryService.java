package kr.co.deepple.report.application;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportQueryService {

  private final ReportQueryRepository reportQueryRepository;
  private final FindMemberSummaryUseCase findMemberSummaryUseCase;

  public Page<ReportView> getPage(ReportSearchCondition condition, Pageable pageable) {
    Page<RawReportView> page = reportQueryRepository.getPage(condition, pageable);
    Set<Long> memberIds =
        page.getContent().stream()
            .flatMap(raw -> Stream.of(raw.reporterId(), raw.reporteeId()))
            .collect(Collectors.toSet());
    Map<Long, MemberSummary> members = findMemberSummaryUseCase.findMemberSummaries(memberIds);
    return page.map(raw ->
        new ReportView(
            raw.id(),
            raw.reporterId(),
            nicknameOf(members, raw.reporterId()),
            raw.reporteeId(),
            nicknameOf(members, raw.reporteeId()),
            raw.reason(),
            raw.result(),
            raw.createdAt()));
  }

  public ReportDetailView getReport(long id) {
    RawReportDetailView raw =
        reportQueryRepository
            .findReportDetailView(id)
            .orElseThrow(
                () -> new DeeppleException(ReportErrorCode.REPORT_NOT_FOUND, "reportId=" + id));
    Map<Long, MemberSummary> members =
        findMemberSummaryUseCase.findMemberSummaries(
            Stream.of(raw.reporterId(), raw.reporteeId()).collect(Collectors.toSet()));
    return new ReportDetailView(
        raw.id(),
        raw.version(),
        raw.reporterId(),
        nicknameOf(members, raw.reporterId()),
        raw.reporteeId(),
        nicknameOf(members, raw.reporteeId()),
        raw.reason(),
        raw.result(),
        raw.content(),
        raw.createdAt());
  }

  private String nicknameOf(Map<Long, MemberSummary> members, Long memberId) {
    MemberSummary member = members.get(memberId);
    return member == null ? null : member.nickname();
  }
}
