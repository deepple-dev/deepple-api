package kr.co.deepple.admin.query.match;

import static kr.co.deepple.match.command.domain.match.QMatch.match;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import kr.co.deepple.member.command.domain.member.QMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MatchHistoryQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Page<MatchHistoryView> findMatchHistories(
      MatchHistorySearchCondition condition, Pageable pageable) {
    QMember requester = new QMember("requester");
    QMember responder = new QMember("responder");

    List<MatchHistoryView> content =
        queryFactory
            .select(
                new QMatchHistoryView(
                    match.id,
                    requester.profile.nickname.value,
                    responder.profile.nickname.value,
                    match.requestMessage.value,
                    match.responseMessage.value,
                    match.status.stringValue(),
                    match.readByResponderAt.stringValue()))
            .from(match)
            .leftJoin(requester)
            .on(requester.id.eq(match.requesterId))
            .leftJoin(responder)
            .on(responder.id.eq(match.responderId))
            .where(
                matchStatusEq(condition.matchStatus()),
                nicknameEq(condition.nickname(), requester, responder),
                phoneNumberEq(condition.phoneNumber(), requester, responder),
                createdAtGoe(condition.startDate()),
                createdAtLoe(condition.endDate()))
            .orderBy(match.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    long totalCount =
        Optional.ofNullable(
                queryFactory
                    .select(match.count())
                    .from(match)
                    .leftJoin(requester)
                    .on(requester.id.eq(match.requesterId))
                    .leftJoin(responder)
                    .on(responder.id.eq(match.responderId))
                    .where(
                        matchStatusEq(condition.matchStatus()),
                        nicknameEq(condition.nickname(), requester, responder),
                        phoneNumberEq(condition.phoneNumber(), requester, responder),
                        createdAtGoe(condition.startDate()),
                        createdAtLoe(condition.endDate()))
                    .fetchOne())
            .orElse(0L);

    return new PageImpl<>(content, pageable, totalCount);
  }

  private BooleanExpression matchStatusEq(String matchStatus) {
    if (matchStatus == null) {
      return null;
    }
    return match.status.stringValue().eq(matchStatus);
  }

  private BooleanExpression nicknameEq(String nickname, QMember requester, QMember responder) {
    if (nickname == null || nickname.isBlank()) {
      return null;
    }
    return requester
        .profile
        .nickname
        .value
        .eq(nickname)
        .or(responder.profile.nickname.value.eq(nickname));
  }

  private BooleanExpression phoneNumberEq(
      String phoneNumber, QMember requester, QMember responder) {
    if (phoneNumber == null || phoneNumber.isBlank()) {
      return null;
    }
    return requester
        .phoneNumber
        .value
        .eq(phoneNumber)
        .or(responder.phoneNumber.value.eq(phoneNumber));
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
