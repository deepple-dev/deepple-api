package kr.co.deepple.community.adapter.out.persistence;

import static kr.co.deepple.community.domain.QSelfIntroduction.selfIntroduction;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.common.infra.s3.S3Uploader;
import kr.co.deepple.community.application.dto.AdminSelfIntroductionSearchCondition;
import kr.co.deepple.community.application.dto.RawAdminSelfIntroductionView;
import kr.co.deepple.community.application.port.out.AdminSelfIntroductionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminSelfIntroductionQueryRepositoryImpl
    implements AdminSelfIntroductionQueryRepository {

  private final JPAQueryFactory queryFactory;
  private final S3Uploader s3Uploader;

  @Override
  public Page<RawAdminSelfIntroductionView> findSelfIntroductions(
      AdminSelfIntroductionSearchCondition condition, Set<Long> memberIds, Pageable pageable) {

    BooleanExpression searchCondition = buildAdminSearchCondition(condition, memberIds);

    List<RawAdminSelfIntroductionView> content =
        queryFactory
            .select(
                selfIntroduction.id,
                selfIntroduction.memberId,
                selfIntroduction.isOpened,
                selfIntroduction.content,
                selfIntroduction.imageKey,
                selfIntroduction.createdAt,
                selfIntroduction.updatedAt,
                selfIntroduction.deletedAt)
            .from(selfIntroduction)
            .where(searchCondition)
            .orderBy(selfIntroduction.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch()
            .stream()
            .map(row ->
                new RawAdminSelfIntroductionView(
                    row.get(selfIntroduction.id),
                    row.get(selfIntroduction.memberId),
                    Boolean.TRUE.equals(row.get(selfIntroduction.isOpened)),
                    row.get(selfIntroduction.content),
                    s3Uploader.getObjectUrl(row.get(selfIntroduction.imageKey)),
                    row.get(selfIntroduction.createdAt),
                    row.get(selfIntroduction.updatedAt),
                    row.get(selfIntroduction.deletedAt)))
            .toList();

    long totalCount =
        Optional.ofNullable(
                queryFactory
                    .select(selfIntroduction.count())
                    .from(selfIntroduction)
                    .where(searchCondition)
                    .fetchOne())
            .orElse(0L);

    return new PageImpl<>(content, pageable, totalCount);
  }

  private BooleanExpression buildAdminSearchCondition(
      AdminSelfIntroductionSearchCondition condition, Set<Long> memberIds) {
    return Expressions.allOf(
        memberIdIn(memberIds),
        isOpenedEq(condition.isOpened()),
        startDateGoe(condition.startDate()),
        loeEndDate(condition.endDate()));
  }

  private BooleanExpression memberIdIn(Set<Long> memberIds) {
    if (memberIds == null) {
      return null;
    }
    return selfIntroduction.memberId.in(memberIds);
  }

  private BooleanExpression isOpenedEq(Boolean isOpened) {
    if (isOpened == null) {
      return null;
    }
    return selfIntroduction.isOpened.eq(isOpened);
  }

  private BooleanExpression startDateGoe(LocalDate startDate) {
    if (startDate == null) {
      return null;
    }
    return selfIntroduction.createdAt.goe(startDate.atStartOfDay());
  }

  private BooleanExpression loeEndDate(LocalDate endDate) {
    if (endDate == null) {
      return null;
    }
    return selfIntroduction.createdAt.lt(endDate.plusDays(1).atStartOfDay());
  }
}
