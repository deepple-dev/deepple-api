package kr.co.deepple.match.adapter.out.persistence;

import static kr.co.deepple.match.domain.QMatch.match;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import kr.co.deepple.match.application.dto.QRawMatchView;
import kr.co.deepple.match.application.dto.RawMatchView;
import kr.co.deepple.match.application.port.out.MatchQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MatchQueryRepositoryImpl implements MatchQueryRepository {

  private static final int PAGE_SIZE = 13;
  private final JPAQueryFactory queryFactory;

  /** 자신이 보낸 메시지 내역 (요청자로 보낸 메시지 + 응답자로 응답한 메시지). */
  @Override
  public List<RawMatchView> findSentMatches(long userId, Long lastMatchId, Set<Long> blockedIds) {
    return queryFactory
        .select(
            new QRawMatchView(
                match.id,
                // 상대방 ID: requester면 responderId, responder면 requesterId
                Expressions.cases()
                    .when(match.requesterId.eq(userId))
                    .then(match.responderId)
                    .otherwise(match.requesterId),
                // 상대방 메시지: requester면 responseMessage, responder면 requestMessage
                Expressions.cases()
                    .when(match.requesterId.eq(userId))
                    .then(match.responseMessage.value)
                    .otherwise(match.requestMessage.value),
                // 내 메시지: requester면 requestMessage, responder면 responseMessage
                Expressions.cases()
                    .when(match.requesterId.eq(userId))
                    .then(match.requestMessage.value)
                    .otherwise(match.responseMessage.value),
                match.status.stringValue(),
                match.createdAt,
                // 상대가 내 요청 메시지를 읽은 시각: 내가 요청자일 때만 readByResponderAt, 아니면 null
                Expressions.cases()
                    .when(match.requesterId.eq(userId))
                    .then(match.readByResponderAt)
                    .otherwise(Expressions.nullExpression(LocalDateTime.class))))
        .from(match)
        .where(
            match
                .requesterId
                .eq(userId)
                .and(match.requestMessage.isNotNull())
                .or(match.responderId.eq(userId).and(match.responseMessage.isNotNull())),
            ltMatchId(lastMatchId),
            opponentNotBlocked(userId, blockedIds))
        .orderBy(match.id.desc())
        .limit(PAGE_SIZE)
        .fetch();
  }

  /** 자신이 받은 메시지 내역 (응답자로 받은 메시지 + 요청자로 보냈는데 상대방이 응답한 메시지). */
  @Override
  public List<RawMatchView> findReceiveMatches(
      long userId, Long lastMatchId, Set<Long> blockedIds) {
    return queryFactory
        .select(
            new QRawMatchView(
                match.id,
                // 상대방 ID: responder면 requesterId, requester면 responderId
                Expressions.cases()
                    .when(match.responderId.eq(userId))
                    .then(match.requesterId)
                    .otherwise(match.responderId),
                // 상대방 메시지: responder면 requestMessage, requester면 responseMessage
                Expressions.cases()
                    .when(match.responderId.eq(userId))
                    .then(match.requestMessage.value)
                    .otherwise(match.responseMessage.value),
                // 내 메시지: responder면 responseMessage, requester면 requestMessage
                Expressions.cases()
                    .when(match.responderId.eq(userId))
                    .then(match.responseMessage.value)
                    .otherwise(match.requestMessage.value),
                match.status.stringValue(),
                match.createdAt,
                Expressions.nullExpression(LocalDateTime.class)))
        .from(match)
        .where(
            match
                .responderId
                .eq(userId)
                .and(match.requestMessage.isNotNull())
                .or(match.requesterId.eq(userId).and(match.responseMessage.isNotNull())),
            ltMatchId(lastMatchId),
            opponentNotBlocked(userId, blockedIds))
        .orderBy(match.id.desc())
        .limit(PAGE_SIZE)
        .fetch();
  }

  private BooleanExpression ltMatchId(Long lastMatchId) {
    return lastMatchId != null ? match.id.lt(lastMatchId) : null;
  }

  private BooleanExpression opponentNotBlocked(long userId, Set<Long> blockedIds) {
    if (blockedIds.isEmpty()) {
      return null;
    }
    // userId가 requester인 경우 responderId가 차단 목록에 없어야 함
    // userId가 responder인 경우 requesterId가 차단 목록에 없어야 함
    return match
        .requesterId
        .eq(userId)
        .and(match.responderId.notIn(blockedIds))
        .or(match.responderId.eq(userId).and(match.requesterId.notIn(blockedIds)));
  }
}
