package kr.co.deepple.like.adapter.out.persistence;

import static kr.co.deepple.like.domain.QLike.like;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Set;
import kr.co.deepple.like.application.dto.QRawLikeView;
import kr.co.deepple.like.application.dto.RawLikeView;
import kr.co.deepple.like.application.port.out.LikeQueryRepository;
import kr.co.deepple.like.domain.QLike;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LikeQueryRepositoryImpl implements LikeQueryRepository {

  private static final int PAGE_SIZE = 13;
  private final JPAQueryFactory queryFactory;

  @Override
  public List<RawLikeView> findSentLikes(long senderId, Long lastLikeId, Set<Long> blockedIds) {
    QLike mutual = new QLike("mutual");

    return queryFactory
        .select(new QRawLikeView(like.id, like.receiverId, mutual.id.isNotNull(), like.createdAt))
        .from(like)
        .leftJoin(mutual)
        .on(mutual.senderId.eq(like.receiverId).and(mutual.receiverId.eq(senderId)))
        .where(eqSender(senderId), ltLikeId(lastLikeId), receiverIdNotIn(blockedIds))
        .orderBy(like.id.desc())
        .limit(PAGE_SIZE)
        .fetch();
  }

  @Override
  public List<RawLikeView> findReceivedLikes(
      long receiverId, Long lastLikeId, Set<Long> blockedIds) {
    QLike mutual = new QLike("mutual");

    return queryFactory
        .select(new QRawLikeView(like.id, like.senderId, mutual.id.isNotNull(), like.createdAt))
        .from(like)
        .leftJoin(mutual)
        .on(mutual.senderId.eq(receiverId).and(mutual.receiverId.eq(like.senderId)))
        .where(eqReceiver(receiverId), ltLikeId(lastLikeId), senderIdNotIn(blockedIds))
        .orderBy(like.id.desc())
        .limit(PAGE_SIZE)
        .fetch();
  }

  private BooleanExpression eqSender(long senderId) {
    return like.senderId.eq(senderId);
  }

  private BooleanExpression eqReceiver(long receiverId) {
    return like.receiverId.eq(receiverId);
  }

  private BooleanExpression ltLikeId(Long lastLikeId) {
    return lastLikeId != null ? like.id.lt(lastLikeId) : null;
  }

  private BooleanExpression senderIdNotIn(Set<Long> ids) {
    if (ids.isEmpty()) {
      return null;
    }
    return like.senderId.notIn(ids);
  }

  private BooleanExpression receiverIdNotIn(Set<Long> ids) {
    if (ids.isEmpty()) {
      return null;
    }
    return like.receiverId.notIn(ids);
  }
}
