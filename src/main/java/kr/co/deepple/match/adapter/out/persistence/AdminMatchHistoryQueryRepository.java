package kr.co.deepple.match.adapter.out.persistence;

import static kr.co.deepple.match.domain.QMatch.match;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.match.application.dto.MatchHistorySearchCondition;
import kr.co.deepple.match.application.dto.QRawMatchHistoryView;
import kr.co.deepple.match.application.dto.RawMatchHistoryView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminMatchHistoryQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Page<RawMatchHistoryView> findMatchHistories(
      MatchHistorySearchCondition condition,
      Set<Long> nicknameMemberIds,
      Set<Long> phoneNumberMemberIds,
      Pageable pageable) {
    BooleanExpression[] predicates = {
      matchStatusEq(condition.matchStatus()),
      participantIn(nicknameMemberIds),
      participantIn(phoneNumberMemberIds),
      createdAtGoe(condition.startDate()),
      createdAtLoe(condition.endDate())
    };

    List<RawMatchHistoryView> content =
        queryFactory
            .select(
                new QRawMatchHistoryView(
                    match.id,
                    match.requesterId,
                    match.responderId,
                    match.requestMessage.value,
                    match.responseMessage.value,
                    match.status.stringValue(),
                    match.readByResponderAt.stringValue()))
            .from(match)
            .where(predicates)
            .orderBy(match.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    long totalCount =
        Optional.ofNullable(
                queryFactory.select(match.count()).from(match).where(predicates).fetchOne())
            .orElse(0L);

    return new PageImpl<>(content, pageable, totalCount);
  }

  private BooleanExpression matchStatusEq(String matchStatus) {
    if (matchStatus == null) {
      return null;
    }
    return match.status.stringValue().eq(matchStatus);
  }

  private BooleanExpression participantIn(Set<Long> memberIds) {
    if (memberIds == null) {
      return null;
    }
    return match.requesterId.in(memberIds).or(match.responderId.in(memberIds));
  }

  private BooleanExpression createdAtGoe(LocalDate startDate) {
    if (startDate == null) {
      return null;
    }
    return match.createdAt.goe(startDate.atStartOfDay());
  }

  private BooleanExpression createdAtLoe(LocalDate endDate) {
    if (endDate == null) {
      return null;
    }
    return match.createdAt.lt(endDate.plusDays(1).atStartOfDay());
  }
}
