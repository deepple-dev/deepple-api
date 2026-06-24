package deepple.deepple.member.query.member.infra;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import deepple.deepple.match.command.domain.match.MatchStatus;
import deepple.deepple.member.command.domain.member.Hobby;
import deepple.deepple.member.query.member.view.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;
import static com.querydsl.core.types.dsl.Expressions.cases;
import static com.querydsl.core.types.dsl.Expressions.enumPath;
import static deepple.deepple.block.domain.QBlock.block;
import static deepple.deepple.community.command.domain.profileexchange.QProfileExchange.profileExchange;
import static deepple.deepple.interview.command.domain.answer.QInterviewAnswer.interviewAnswer;
import static deepple.deepple.interview.command.domain.question.QInterviewQuestion.interviewQuestion;
import static deepple.deepple.like.command.domain.QLike.like;
import static deepple.deepple.match.command.domain.match.QMatch.match;
import static deepple.deepple.member.command.domain.introduction.QMemberIntroduction.memberIntroduction;
import static deepple.deepple.member.command.domain.member.QMember.member;
import static deepple.deepple.member.command.domain.profileImage.QProfileImage.profileImage;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {
    private final JPAQueryFactory queryFactory;

    public Optional<MemberInfoView> findInfoByMemberId(Long memberId) {
        EnumPath<Hobby> hobby = enumPath(Hobby.class, "hobbyAlias");

        MemberInfoView memberInfoView = queryFactory.from(member)
            .leftJoin(member.profile.hobbies, hobby)
            .leftJoin(interviewAnswer)
            .on(member.id.eq(interviewAnswer.memberId))
            .leftJoin(interviewQuestion)
            .on(interviewQuestion.id.eq(interviewAnswer.questionId).and(interviewQuestion.isPublic.eq(true)))
            .where(member.id.eq(memberId))
            .transform(groupBy(member.id).as(
                new QMemberInfoView(member.id, member.activityStatus.stringValue(), member.isVip.isTrue(),
                    member.primaryContactType.stringValue(), member.isDatingExamSubmitted.isTrue(),
                    member.profile.nickname.value.stringValue(),
                    member.profile.gender.stringValue(), member.kakaoId.value, member.profile.yearOfBirth.value,
                    member.profile.height, member.phoneNumber.value, member.profile.job.stringValue(),
                    member.profile.highestEducation.stringValue(), member.profile.region.city.stringValue(),
                    member.profile.region.district.stringValue(), member.profile.mbti.stringValue(),
                    member.profile.smokingStatus.stringValue(), member.profile.drinkingStatus.stringValue(),
                    member.profile.religion.stringValue(), set(hobby.stringValue()),
                    set((new QInterviewInfoView(interviewQuestion.id, interviewQuestion.content,
                        interviewAnswer.content))))))
            .get(memberId);

        return Optional.ofNullable(memberInfoView);
    }

    public Optional<MemberProfileView> findProfileByMemberId(Long memberId) {
        EnumPath<Hobby> hobby = enumPath(Hobby.class, "hobbyAlias");

        MemberProfileView memberProfileView = queryFactory.from(member)
            .leftJoin(member.profile.hobbies, hobby)
            .transform(groupBy(member.id).as(
                new QMemberProfileView(member.profile.nickname.value, member.profile.yearOfBirth.value,
                    member.profile.gender.stringValue(), member.profile.height, member.profile.job.stringValue(),
                    set(hobby.stringValue()), member.profile.mbti.stringValue(),
                    member.profile.region.city.stringValue(), member.profile.region.district.stringValue(),
                    member.profile.smokingStatus.stringValue(), member.profile.drinkingStatus.stringValue(),
                    member.profile.highestEducation.stringValue(), member.profile.religion.stringValue())))
            .get(memberId);
        return Optional.ofNullable(memberProfileView);
    }

    public Optional<MemberContactView> findContactsByMemberId(Long memberId) {
        MemberContactView memberContactView = queryFactory.select(
            new QMemberContactView(member.phoneNumber.value, member.kakaoId.value,
                member.primaryContactType.stringValue())).from(member).where(member.id.eq(memberId)).fetchOne();

        return Optional.ofNullable(memberContactView);
    }

    public Optional<OtherMemberProfileView> findOtherProfileByMemberId(Long memberId, Long otherMemberId) {
        EnumPath<Hobby> hobby = enumPath(Hobby.class, "hobbyAlias");

        OtherMemberProfileView otherMemberProfileView = queryFactory.from(member)
            .leftJoin(member.profile.hobbies, hobby)
            .leftJoin(profileImage)
            .on(profileImage.memberId.eq(otherMemberId).and(profileImage.isPrimary.eq(true)))
            .leftJoin(match)
            .on(getMatchJoinCondition(memberId, otherMemberId))
            .leftJoin(profileExchange)
            .on(getProfileExchangeJoinCondition(memberId, otherMemberId))
            .leftJoin(like)
            .on(like.senderId.eq(memberId).and(like.receiverId.eq(otherMemberId)))
            .leftJoin(memberIntroduction)
            .on(memberIntroduction.memberId.eq(memberId).and(memberIntroduction.introducedMemberId.eq(otherMemberId)))
            .where(member.id.eq(otherMemberId))
            .transform(groupBy(member.id).as(
                new QOtherMemberProfileView(member.id, member.profile.nickname.value, profileImage.imageUrl.value,
                    member.profile.yearOfBirth.value, member.profile.gender.stringValue(), member.profile.height,
                    member.profile.job.stringValue(), set(hobby.stringValue()), member.profile.mbti.stringValue(),
                    member.profile.region.city.stringValue(), member.profile.region.district.stringValue(),
                    member.profile.smokingStatus.stringValue(), member.profile.drinkingStatus.stringValue(),
                    member.profile.highestEducation.stringValue(), member.profile.religion.stringValue(),
                    like.level.stringValue(),
                    member.lastAccessedAt,
                    match.id, match.requesterId, match.responderId, match.requestMessage.value,
                    match.responseMessage.value, match.status.stringValue(), match.requesterContactType.stringValue(),
                    match.responderContactType.stringValue(),
                    member.phoneNumber.value, member.kakaoId.value,
                    profileExchange.id, profileExchange.requesterId, profileExchange.responderId,
                    profileExchange.status.stringValue(), memberIntroduction.type.stringValue()
                )))
            .get(otherMemberId);


        return Optional.ofNullable(otherMemberProfileView);
    }

    public Optional<ProfileAccessView> findProfileAccessViewByMemberId(Long memberId, Long otherMemberId) {
        ProfileAccessView view = queryFactory.select(
                new QProfileAccessView(
                    cases().when(memberIntroduction.id.isNotNull()).then(true).otherwise(false),
                    match.requesterId,
                    match.responderId,
                    profileExchange.requesterId,
                    profileExchange.responderId,
                    profileExchange.status.stringValue(),
                    cases().when(like.id.isNotNull()).then(true).otherwise(false),
                    block.id.isNotNull(),
                    member.activityStatus.stringValue()
                ))
            .from(member)
            .leftJoin(memberIntroduction)
            .on(memberIntroduction.introducedMemberId.eq(otherMemberId).and(memberIntroduction.memberId.eq(memberId)))
            .leftJoin(profileExchange)
            .on(getProfileExchangeJoinCondition(memberId, otherMemberId))
            .leftJoin(match)
            .on(getMatchJoinConditionForProfile(memberId, otherMemberId))
            .leftJoin(like)
            .on(like.receiverId.eq(memberId).and(like.senderId.eq(otherMemberId)))
            .leftJoin(block)
            .on(block.blockerId.eq(otherMemberId).and(block.blockedId.eq(memberId)))
            .where(member.id.eq(otherMemberId))
            .fetchOne();

        return Optional.ofNullable(view);
    }

    public List<InterviewResultView> findInterviewsByMemberId(Long memberId) {
        return queryFactory.from(interviewQuestion)
            .innerJoin(interviewAnswer)
            .on(getInterviewAnswerJoinCondition(memberId))
            .select(new QInterviewResultView(interviewQuestion.content, interviewQuestion.category.stringValue(),
                interviewAnswer.content))
            .where(interviewQuestion.isPublic.eq(true))
            .fetch();
    }

    private BooleanExpression getProfileExchangeJoinCondition(Long memberId, Long otherMemberId) {
        return (profileExchange.requesterId.eq(memberId).and(profileExchange.responderId.eq(otherMemberId)))
            .or(profileExchange.requesterId.eq(otherMemberId).and(profileExchange.responderId.eq(memberId)));
    }

    private BooleanExpression getMatchJoinCondition(Long memberId, Long otherMemberId) {
        return (match.requesterId.eq(memberId)
            .and(match.responderId.eq(otherMemberId))
            .or(match.requesterId.eq(otherMemberId).and(match.responderId.eq(memberId)))).and(
            match.status.notIn(MatchStatus.REJECT_CHECKED, MatchStatus.EXPIRED));
    }

    private BooleanExpression getMatchJoinConditionForProfile(Long memberId, Long otherMemberId) {
        return match.requesterId.eq(otherMemberId)
            .and(match.responderId.eq(memberId))
            .and(match.status.notIn(MatchStatus.REJECT_CHECKED, MatchStatus.REJECTED));
    }

    private BooleanExpression getInterviewAnswerJoinCondition(Long memberId) {
        return interviewQuestion.id.eq(interviewAnswer.questionId).and(interviewAnswer.memberId.eq(memberId));
    }

    public Optional<HeartBalanceView> findHeartBalanceByMemberId(long memberId) {
        return Optional.ofNullable(queryFactory.select(
                new QHeartBalanceView(member.heartBalance.purchaseHeartBalance, member.heartBalance.missionHeartBalance,
                    member.heartBalance.purchaseHeartBalance.add(member.heartBalance.missionHeartBalance)))
            .from(member)
            .where(member.id.eq(memberId))
            .fetchOne());
    }
}
