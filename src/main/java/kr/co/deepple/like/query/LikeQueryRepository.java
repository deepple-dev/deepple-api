package kr.co.deepple.like.query;

import static kr.co.deepple.block.domain.QBlock.block;
import static kr.co.deepple.like.command.domain.QLike.like;
import static kr.co.deepple.member.command.domain.member.QMember.member;
import static kr.co.deepple.member.command.domain.profileImage.QProfileImage.profileImage;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.like.command.domain.QLike;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LikeQueryRepository {

  private static final int PAGE_SIZE = 13;
  private final JPAQueryFactory queryFactory;

  public List<RawLikeView> findSentLikes(long senderId, Long lastLikeId) {
    QLike mutual = new QLike("mutual");

    Set<Long> blockedIds = getBlockedIds(senderId);

    return queryFactory
        .select(
            new QRawLikeView(
                like.id,
                like.receiverId,
                profileImage.imageUrl.value,
                member.profile.nickname.value,
                member.profile.region.city.stringValue(),
                member.profile.yearOfBirth.value,
                mutual.id.isNotNull(),
                like.createdAt))
        .from(like)
        .join(member)
        .on(member.id.eq(like.receiverId))
        .leftJoin(profileImage)
        .on(profileImage.memberId.eq(member.id).and(profileImage.isPrimary.eq(true)))
        .leftJoin(mutual)
        .on(mutual.senderId.eq(like.receiverId).and(mutual.receiverId.eq(senderId)))
        .where(eqSender(senderId), ltLikeId(lastLikeId), receiverIdNotIn(blockedIds))
        .orderBy(like.id.desc())
        .limit(PAGE_SIZE)
        .fetch();
  }

  public List<RawLikeView> findReceivedLikes(long receiverId, Long lastLikeId) {
    QLike mutual = new QLike("mutual");

    Set<Long> blockedIds = getBlockedIds(receiverId);

    return queryFactory
        .select(
            new QRawLikeView(
                like.id,
                like.senderId,
                profileImage.imageUrl.value,
                member.profile.nickname.value,
                member.profile.region.city.stringValue(),
                member.profile.yearOfBirth.value,
                mutual.id.isNotNull(),
                like.createdAt))
        .from(like)
        .join(member)
        .on(member.id.eq(like.senderId))
        .leftJoin(profileImage)
        .on(profileImage.memberId.eq(member.id).and(profileImage.isPrimary.eq(true)))
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

  private Set<Long> getBlockedIds(long blockerId) {
    return queryFactory
        .select(block.blockedId)
        .from(block)
        .where(block.blockerId.eq(blockerId))
        .fetch()
        .stream()
        .collect(Collectors.toSet());
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
