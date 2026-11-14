package kr.co.deepple.admin.adapter.out.persistence;

import static kr.co.deepple.admin.domain.QScreening.screening;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.admin.application.dto.QRawScreeningView;
import kr.co.deepple.admin.application.dto.RawScreeningView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScreeningQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Page<RawScreeningView> findScreenings(
      String screeningStatus, Set<Long> memberIds, Pageable pageable) {
    List<RawScreeningView> content =
        queryFactory
            .select(
                new QRawScreeningView(
                    screening.id,
                    screening.memberId,
                    screening.status.stringValue(),
                    screening.rejectionReason.stringValue()))
            .from(screening)
            .where(screeningStatusEq(screeningStatus), memberIdIn(memberIds))
            .orderBy(screening.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    long totalCount =
        Optional.ofNullable(
                queryFactory
                    .select(screening.count())
                    .from(screening)
                    .where(screeningStatusEq(screeningStatus), memberIdIn(memberIds))
                    .fetchOne())
            .orElse(0L);

    return new PageImpl<>(content, pageable, totalCount);
  }

  private BooleanExpression screeningStatusEq(String screeningStatus) {
    if (screeningStatus == null) {
      return null;
    }
    return screening.status.stringValue().eq(screeningStatus);
  }

  private BooleanExpression memberIdIn(Set<Long> memberIds) {
    if (memberIds == null) {
      return null;
    }
    return screening.memberId.in(memberIds);
  }
}
