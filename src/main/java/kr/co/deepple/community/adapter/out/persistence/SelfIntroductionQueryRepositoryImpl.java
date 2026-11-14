package kr.co.deepple.community.adapter.out.persistence;

import static kr.co.deepple.community.domain.QProfileExchange.profileExchange;
import static kr.co.deepple.community.domain.QSelfIntroduction.selfIntroduction;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.community.application.dto.QRawSelfIntroductionView;
import kr.co.deepple.community.application.dto.RawSelfIntroductionView;
import kr.co.deepple.community.application.port.out.SelfIntroductionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SelfIntroductionQueryRepositoryImpl implements SelfIntroductionQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<RawSelfIntroductionView> findSelfIntroductions(
      long memberId, Long lastId, Set<Long> blockedIds, int limit) {
    return queryFactory
        .select(rawSelfIntroductionView())
        .from(selfIntroduction)
        .where(
            selfIntroduction.deletedAt.isNull(),
            selfIntroduction.isOpened.isTrue().or(selfIntroduction.memberId.eq(memberId)),
            ltId(lastId),
            memberIdNotIn(blockedIds))
        .orderBy(selfIntroduction.id.desc())
        .limit(limit)
        .fetch();
  }

  @Override
  public List<RawSelfIntroductionView> findMySelfIntroductions(
      long memberId, Long lastId, int limit) {
    return queryFactory
        .select(rawSelfIntroductionView())
        .from(selfIntroduction)
        .where(
            selfIntroduction.deletedAt.isNull(),
            selfIntroduction.memberId.eq(memberId),
            ltId(lastId))
        .orderBy(selfIntroduction.id.desc())
        .limit(limit)
        .fetch();
  }

  @Override
  public Optional<RawSelfIntroductionView> findSelfIntroduction(long id) {
    return Optional.ofNullable(
        queryFactory
            .select(rawSelfIntroductionView())
            .from(selfIntroduction)
            .where(selfIntroduction.id.eq(id), selfIntroduction.deletedAt.isNull())
            .fetchOne());
  }

  @Override
  public Optional<String> findProfileExchangeStatus(long memberId, long otherMemberId) {
    return Optional.ofNullable(
        queryFactory
            .select(profileExchange.status.stringValue())
            .from(profileExchange)
            .where(
                profileExchange
                    .requesterId
                    .eq(memberId)
                    .and(profileExchange.responderId.eq(otherMemberId))
                    .or(
                        profileExchange
                            .requesterId
                            .eq(otherMemberId)
                            .and(profileExchange.responderId.eq(memberId))))
            .orderBy(profileExchange.id.desc())
            .fetchFirst());
  }

  private QRawSelfIntroductionView rawSelfIntroductionView() {
    return new QRawSelfIntroductionView(
        selfIntroduction.id,
        selfIntroduction.memberId,
        selfIntroduction.title,
        selfIntroduction.content,
        selfIntroduction.imageUrl,
        selfIntroduction.createdAt);
  }

  private BooleanExpression ltId(Long lastId) {
    return lastId != null ? selfIntroduction.id.lt(lastId) : null;
  }

  private BooleanExpression memberIdNotIn(Set<Long> memberIds) {
    if (memberIds.isEmpty()) {
      return null;
    }
    return selfIntroduction.memberId.notIn(memberIds);
  }
}
