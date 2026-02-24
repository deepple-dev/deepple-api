package deepple.deepple.match.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static deepple.deepple.block.domain.QBlock.block;
import static deepple.deepple.match.command.domain.match.QMatch.match;
import static deepple.deepple.member.command.domain.member.QMember.member;
import static deepple.deepple.member.command.domain.profileImage.QProfileImage.profileImage;

@Repository
@RequiredArgsConstructor
public class MatchQueryRepository {
    private static final int PAGE_SIZE = 13;
    private final JPAQueryFactory queryFactory;

    /**
     * 자신이 보낸 메시지 내역 (요청자로 보낸 메시지 + 응답자로 응답한 메시지).
     */
    public List<MatchView> findSentMatches(long userId, Long lastMatchId) {
        Set<Long> blockedIds = getBlockedIds(userId);

        return queryFactory
            .select(new QMatchView(
                match.id,
                // 상대방 ID: requester면 responderId, responder면 requesterId
                Expressions.cases()
                    .when(match.requesterId.eq(userId)).then(match.responderId)
                    .otherwise(match.requesterId),
                // 상대방 메시지: requester면 responseMessage, responder면 requestMessage
                Expressions.cases()
                    .when(match.requesterId.eq(userId)).then(match.responseMessage.value)
                    .otherwise(match.requestMessage.value),
                member.profile.nickname.value,
                profileImage.imageUrl.value,
                member.profile.region.city.stringValue(),
                // 내 메시지: requester면 requestMessage, responder면 responseMessage
                Expressions.cases()
                    .when(match.requesterId.eq(userId)).then(match.requestMessage.value)
                    .otherwise(match.responseMessage.value),
                match.status.stringValue(),
                match.createdAt
            ))
            .from(match)
            .join(member).on(
                match.requesterId.eq(userId).and(member.id.eq(match.responderId))
                    .or(match.responderId.eq(userId).and(member.id.eq(match.requesterId)))
            )
            .leftJoin(profileImage).on(profileImage.memberId.eq(member.id).and(profileImage.isPrimary.eq(true)))
            .where(
                match.requesterId.eq(userId).and(match.requestMessage.isNotNull())
                    .or(match.responderId.eq(userId).and(match.responseMessage.isNotNull())),
                ltMatchId(lastMatchId),
                opponentNotBlocked(userId, blockedIds)
            )
            .orderBy(match.id.desc())
            .limit(PAGE_SIZE)
            .fetch();
    }

    /**
     * 자신이 받은 메시지 내역 (응답자로 받은 메시지 + 요청자로 보냈는데 상대방이 응답한 메시지).
     */
    public List<MatchView> findReceiveMatches(long userId, Long lastMatchId) {
        Set<Long> blockedIds = getBlockedIds(userId);

        return queryFactory
            .select(new QMatchView(
                match.id,
                // 상대방 ID: responder면 requesterId, requester면 responderId
                Expressions.cases()
                    .when(match.responderId.eq(userId)).then(match.requesterId)
                    .otherwise(match.responderId),
                // 상대방 메시지: responder면 requestMessage, requester면 responseMessage
                Expressions.cases()
                    .when(match.responderId.eq(userId)).then(match.requestMessage.value)
                    .otherwise(match.responseMessage.value),
                member.profile.nickname.value,
                profileImage.imageUrl.value,
                member.profile.region.city.stringValue(),
                // 내 메시지: responder면 responseMessage, requester면 requestMessage
                Expressions.cases()
                    .when(match.responderId.eq(userId)).then(match.responseMessage.value)
                    .otherwise(match.requestMessage.value),
                match.status.stringValue(),
                match.createdAt
            ))
            .from(match)
            .join(member).on(
                match.responderId.eq(userId).and(member.id.eq(match.requesterId))
                    .or(match.requesterId.eq(userId).and(member.id.eq(match.responderId)))
            )
            .leftJoin(profileImage).on(profileImage.memberId.eq(member.id).and(profileImage.isPrimary.eq(true)))
            .where(
                match.responderId.eq(userId).and(match.requestMessage.isNotNull())
                    .or(match.requesterId.eq(userId).and(match.responseMessage.isNotNull())),
                ltMatchId(lastMatchId),
                opponentNotBlocked(userId, blockedIds)
            )
            .orderBy(match.id.desc())
            .limit(PAGE_SIZE)
            .fetch();
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

    private BooleanExpression ltMatchId(Long lastMatchId) {
        return lastMatchId != null ? match.id.lt(lastMatchId) : null;
    }

    private BooleanExpression opponentNotBlocked(long userId, Set<Long> blockedIds) {
        if (blockedIds.isEmpty()) {
            return null;
        }
        // userId가 requester인 경우 responderId가 차단 목록에 없어야 함
        // userId가 responder인 경우 requesterId가 차단 목록에 없어야 함
        return match.requesterId.eq(userId).and(match.responderId.notIn(blockedIds))
            .or(match.responderId.eq(userId).and(match.requesterId.notIn(blockedIds)));
    }
}
