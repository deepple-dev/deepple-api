package deepple.deepple.member.query.introduction.intra;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import deepple.deepple.member.command.domain.member.Hobby;
import deepple.deepple.member.query.introduction.application.IntroductionSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

import static com.querydsl.core.types.dsl.Expressions.enumPath;
import static deepple.deepple.block.domain.QBlock.block;
import static deepple.deepple.like.command.domain.QLike.like;
import static deepple.deepple.match.command.domain.match.QMatch.match;
import static deepple.deepple.member.command.domain.introduction.QMemberIntroduction.memberIntroduction;
import static deepple.deepple.member.command.domain.member.QMember.member;
import static deepple.deepple.member.command.domain.profileImage.QProfileImage.profileImage;

@Repository
@RequiredArgsConstructor
public class IntroductionQueryRepository {
    private final JPAQueryFactory queryFactory;

    public Set<Long> findAllMatchRequestedMemberId(long memberId) {
        return new HashSet<>(queryFactory
            .select(match.requesterId)
            .from(match)
            .where(match.responderId.eq(memberId))
            .fetch());
    }

    public Set<Long> findAllMatchRequestingMemberId(long memberId) {
        return new HashSet<>(queryFactory
            .select(match.responderId)
            .from(match)
            .where(match.requesterId.eq(memberId))
            .fetch());
    }

    public Set<Long> findAllIntroducedMemberId(long memberId) {
        return new HashSet<>(queryFactory
            .select(memberIntroduction.introducedMemberId)
            .from(memberIntroduction)
            .where(memberIntroduction.memberId.eq(memberId))
            .fetch());
    }

    public Set<Long> findAllIntroductionMemberId(IntroductionSearchCondition condition, final long limit) {
        JPAQuery<Long> query = queryFactory
            .select(member.id)
            .from(member)
            .where(
                idsNotIn(condition.getExcludedMemberIds()),
                ageBetween(condition.getMinAge(), condition.getMaxAge()),
                cityIn(condition.getCities()),
                religionEq(condition.getReligion()),
                smokingStatusEq(condition.getSmokingStatus()),
                drinkingStatusEq(condition.getDrinkingStatus()),
                gradeEq(condition.getMemberGrade()),
                genderEq(condition.getGender()),
                createdAtGoe(condition.getJoinedAfter()),
                activityStatusEq(condition.getActivityStatus()),
                isProfilePublicIsTrue()
            )
            .orderBy(member.id.desc())
            .limit(limit);

        applyHobbiesCondition(query, condition);
        return new HashSet<>(query.fetch());
    }

    public List<MemberIntroductionProfileQueryResult> findAllMemberIntroductionProfileQueryResultByMemberIds(
        long memberId, Set<Long> memberIds) {
        EnumPath<Hobby> hobby = enumPath(Hobby.class, "hobbyAlias");

        return new ArrayList<>(queryFactory
            .from(member)
            .leftJoin(memberIntroduction).on(memberIntroduction.memberId.eq(memberId)
                .and(memberIntroduction.introducedMemberId.eq(member.id)))
            .leftJoin(profileImage).on(profileImage.memberId.eq(member.id).and(profileImage.isPrimary.isTrue()))
            .leftJoin(like).on(like.senderId.eq(memberId).and(like.receiverId.eq(member.id)))
            .leftJoin(member.profile.hobbies, hobby)
            .where(member.id.in(memberIds))
            .orderBy(member.id.desc())
            .transform(GroupBy.groupBy(member.id).as(
                new QMemberIntroductionProfileQueryResult(
                    member.id,
                    profileImage.imageUrl.value,
                    member.profile.yearOfBirth.value,
                    member.profile.nickname.value,
                    member.profile.region.city.stringValue(),
                    member.profile.region.district.stringValue(),
                    GroupBy.set(hobby.stringValue()),
                    member.profile.religion.stringValue(),
                    member.profile.mbti.stringValue(),
                    like.level.stringValue(),
                    memberIntroduction.introducedMemberId.isNotNull()
                )
            )).values());
    }

    private BooleanExpression idsNotIn(Set<Long> id) {
        if (id.isEmpty()) {
            return null;
        }
        return member.id.notIn(id);
    }

    private BooleanExpression ageBetween(Integer minAge, Integer maxAge) {
        if (minAge == null && maxAge == null) {
            return null;
        }

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        if (minAge == null) {
            return member.profile.yearOfBirth.value.goe(currentYear - maxAge + 1);
        }
        if (maxAge == null) {
            return member.profile.yearOfBirth.value.loe(currentYear - minAge + 1);
        }

        return member.profile.yearOfBirth.value.between(currentYear - maxAge + 1, currentYear - minAge + 1);
    }

    private BooleanExpression cityIn(Set<String> cities) {
        if (cities == null || cities.isEmpty()) {
            return null;
        }
        return member.profile.region.city.stringValue().in(cities);
    }

    private BooleanExpression religionEq(String religion) {
        if (religion == null) {
            return null;
        }
        return member.profile.religion.stringValue().eq(religion);
    }

    private BooleanExpression smokingStatusEq(String smokingStatus) {
        if (smokingStatus == null) {
            return null;
        }
        return member.profile.smokingStatus.stringValue().eq(smokingStatus);
    }

    private BooleanExpression drinkingStatusEq(String drinkingStatus) {
        if (drinkingStatus == null) {
            return null;
        }
        return member.profile.drinkingStatus.stringValue().eq(drinkingStatus);
    }

    private BooleanExpression gradeEq(String grade) {
        if (grade == null) {
            return null;
        }
        return member.grade.stringValue().eq(grade);
    }

    private BooleanExpression genderEq(String gender) {
        if (gender == null) {
            return null;
        }
        return member.profile.gender.stringValue().eq(gender);
    }

    private BooleanExpression createdAtGoe(LocalDateTime createdAt) {
        if (createdAt == null) {
            return null;
        }
        return member.createdAt.goe(createdAt);
    }

    private BooleanExpression activityStatusEq(String activityStatus) {
        if (activityStatus == null) {
            return null;
        }
        return member.activityStatus.stringValue().eq(activityStatus);
    }

    private BooleanExpression isProfilePublicIsTrue() {
        return member.isProfilePublic.isTrue();
    }

    private void applyHobbiesCondition(JPAQuery<?> query, IntroductionSearchCondition condition) {
        if (condition.getHobbies() != null && !condition.getHobbies().isEmpty()) {
            EnumPath<Hobby> hobby = enumPath(Hobby.class, "hobbyAlias");
            query.join(member.profile.hobbies, hobby)
                .where(hobby.stringValue().in(condition.getHobbies()))
                .distinct();
        }
    }

    public Set<Long> findAllBlockedMemberId(long memberId) {
        return new HashSet<>(queryFactory
            .select(block.blockedId)
            .from(block)
            .where(block.blockerId.eq(memberId))
            .fetch());
    }

    public Set<Long> findAllBlockingMemberId(long memberId) {
        return new HashSet<>(queryFactory
            .select(block.blockerId)
            .from(block)
            .where(block.blockedId.eq(memberId))
            .fetch());
    }
}
