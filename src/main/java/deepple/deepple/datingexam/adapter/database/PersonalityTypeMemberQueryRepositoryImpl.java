package deepple.deepple.datingexam.adapter.database;

import com.querydsl.jpa.impl.JPAQueryFactory;
import deepple.deepple.datingexam.application.required.PersonalityTypeMemberQueryRepository;
import deepple.deepple.datingexam.domain.AnswerPersonalityType;
import deepple.deepple.member.command.domain.member.ActivityStatus;
import deepple.deepple.member.command.domain.member.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static deepple.deepple.block.domain.QBlock.block;
import static deepple.deepple.datingexam.domain.QDatingExamSubmitResult.datingExamSubmitResult;
import static deepple.deepple.member.command.domain.member.QMember.member;

@Repository
@RequiredArgsConstructor
public class PersonalityTypeMemberQueryRepositoryImpl implements PersonalityTypeMemberQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findMemberIdsByDominantType(Long memberId, AnswerPersonalityType type) {
        Set<Long> typeMemberIds = getMemberIdsByDominantType(type);
        typeMemberIds.removeAll(getExcludedMemberIds(memberId));

        if (typeMemberIds.isEmpty()) {
            return Collections.emptyList();
        }

        Gender gender = queryFactory
            .select(member.profile.gender)
            .from(member)
            .where(member.id.eq(memberId))
            .fetchOne();

        if (gender == null) {
            throw new IllegalStateException("멤버의 성별이 null입니다. 멤버 ID: " + memberId);
        }

        return queryFactory
            .select(member.id)
            .from(member)
            .where(member.id.in(typeMemberIds)
                .and(member.profile.gender.eq(gender.getOpposite()))
                .and(member.isProfilePublic.isTrue())
                .and(member.activityStatus.eq(ActivityStatus.ACTIVE))
                .and(member.id.ne(memberId))
            )
            .orderBy(member.id.desc())
            .fetch();
    }

    private Set<Long> getMemberIdsByDominantType(AnswerPersonalityType type) {
        return new HashSet<>(queryFactory
            .select(datingExamSubmitResult.memberId)
            .from(datingExamSubmitResult)
            .where(datingExamSubmitResult.dominantPersonalityType.eq(type))
            .fetch());
    }

    private Set<Long> getExcludedMemberIds(Long memberId) {
        List<Long> blockedIds = queryFactory
            .select(block.blockedId)
            .from(block)
            .where(block.blockerId.eq(memberId))
            .fetch();

        List<Long> blockerIds = queryFactory
            .select(block.blockerId)
            .from(block)
            .where(block.blockedId.eq(memberId))
            .fetch();

        return Stream.concat(
            Stream.concat(blockedIds.stream(), blockerIds.stream()),
            Stream.of(memberId)
        ).collect(java.util.stream.Collectors.toSet());
    }
}
