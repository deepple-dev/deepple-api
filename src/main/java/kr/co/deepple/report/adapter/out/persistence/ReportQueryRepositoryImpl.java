package kr.co.deepple.report.adapter.out.persistence;

import static kr.co.deepple.report.domain.QReport.report;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import kr.co.deepple.report.application.dto.QRawReportDetailView;
import kr.co.deepple.report.application.dto.QRawReportView;
import kr.co.deepple.report.application.dto.RawReportDetailView;
import kr.co.deepple.report.application.dto.RawReportView;
import kr.co.deepple.report.application.dto.ReportSearchCondition;
import kr.co.deepple.report.application.port.out.ReportQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReportQueryRepositoryImpl implements ReportQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<RawReportView> getPage(ReportSearchCondition condition, Pageable pageable) {
    List<RawReportView> views =
        queryFactory
            .select(
                new QRawReportView(
                    report.id,
                    report.reporterId,
                    report.reporteeId,
                    report.reason.stringValue(),
                    report.result.stringValue(),
                    report.createdAt))
            .from(report)
            .where(
                resultEq(condition.result()),
                createdAtGoe(condition.createdAtGoe()),
                createdAtLoe(condition.createdAtLoe()))
            .orderBy(report.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    long totalCount =
        queryFactory
            .select(report.count())
            .from(report)
            .where(
                resultEq(condition.result()),
                createdAtGoe(condition.createdAtGoe()),
                createdAtLoe(condition.createdAtLoe()))
            .fetchOne();

    return new PageImpl<>(views, pageable, totalCount);
  }

  private BooleanExpression resultEq(String result) {
    return result != null ? report.result.stringValue().eq(result) : null;
  }

  private BooleanExpression createdAtGoe(LocalDate createdDateGoe) {
    return createdDateGoe != null ? report.createdAt.goe(createdDateGoe.atStartOfDay()) : null;
  }

  private BooleanExpression createdAtLoe(LocalDate createdDateLoe) {
    return createdDateLoe != null
        ? report.createdAt.loe(createdDateLoe.plusDays(1).atStartOfDay().minusSeconds(1))
        : null;
  }

  @Override
  public Optional<RawReportDetailView> findReportDetailView(long id) {
    RawReportDetailView view =
        queryFactory
            .select(
                new QRawReportDetailView(
                    report.id,
                    report.version,
                    report.reporterId,
                    report.reporteeId,
                    report.reason.stringValue(),
                    report.result.stringValue(),
                    report.content,
                    report.createdAt))
            .from(report)
            .where(idEq(id))
            .fetchOne();

    return Optional.ofNullable(view);
  }

  private BooleanExpression idEq(long id) {
    return report.id.eq(id);
  }
}
