package kr.co.deepple.report.adapter.out.persistence;

import static kr.co.deepple.report.domain.QReport.report;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import kr.co.deepple.member.domain.QMember;
import kr.co.deepple.report.application.dto.QReportDetailView;
import kr.co.deepple.report.application.dto.QReportView;
import kr.co.deepple.report.application.dto.ReportDetailView;
import kr.co.deepple.report.application.dto.ReportSearchCondition;
import kr.co.deepple.report.application.dto.ReportView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReportQueryRepository {

  private static final QMember reporterMember = new QMember("reporterMember");
  private static final QMember reporteeMember = new QMember("reporteeMember");
  private final JPAQueryFactory queryFactory;

  public Page<ReportView> getPage(ReportSearchCondition condition, Pageable pageable) {
    List<ReportView> views =
        queryFactory
            .select(
                new QReportView(
                    report.id,
                    report.reporterId,
                    reporterMember.profile.nickname.value,
                    report.reporteeId,
                    reporteeMember.profile.nickname.value,
                    report.reason.stringValue(),
                    report.result.stringValue(),
                    report.createdAt))
            .from(report)
            .where(
                resultEq(condition.result()),
                createdAtGoe(condition.createdAtGoe()),
                createdAtLoe(condition.createdAtLoe()))
            .leftJoin(reporterMember)
            .on(report.reporterId.eq(reporterMember.id))
            .leftJoin(reporteeMember)
            .on(report.reporteeId.eq(reporteeMember.id))
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
            .leftJoin(reporterMember)
            .on(report.reporterId.eq(reporterMember.id))
            .leftJoin(reporteeMember)
            .on(report.reporteeId.eq(reporteeMember.id))
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

  public Optional<ReportDetailView> findReportDetailView(long id) {
    ReportDetailView view =
        queryFactory
            .select(
                new QReportDetailView(
                    report.id,
                    report.version,
                    report.reporterId,
                    reporterMember.profile.nickname.value,
                    report.reporteeId,
                    reporteeMember.profile.nickname.value,
                    report.reason.stringValue(),
                    report.result.stringValue(),
                    report.content,
                    report.createdAt))
            .from(report)
            .where(idEq(id))
            .leftJoin(reporterMember)
            .on(report.reporterId.eq(reporterMember.id))
            .leftJoin(reporteeMember)
            .on(report.reporteeId.eq(reporteeMember.id))
            .fetchOne();

    return Optional.ofNullable(view);
  }

  private BooleanExpression idEq(long id) {
    return report.id.eq(id);
  }
}
