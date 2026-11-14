package kr.co.deepple.member.adapter.out.persistence;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;
import static com.querydsl.core.types.dsl.Expressions.cases;
import static com.querydsl.core.types.dsl.Expressions.enumPath;
import static kr.co.deepple.block.domain.QBlock.block;
import static kr.co.deepple.community.domain.QProfileExchange.profileExchange;
import static kr.co.deepple.interview.domain.QInterviewAnswer.interviewAnswer;
import static kr.co.deepple.interview.domain.QInterviewQuestion.interviewQuestion;
import static kr.co.deepple.like.domain.QLike.like;
import static kr.co.deepple.match.domain.QMatch.match;
import static kr.co.deepple.member.domain.QMember.member;
import static kr.co.deepple.member.domain.QProfileImage.profileImage;
import static kr.co.deepple.recommendation.domain.QMemberIntroduction.memberIntroduction;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.match.domain.MatchStatus;
import kr.co.deepple.member.application.dto.InterviewResultView;
import kr.co.deepple.member.application.dto.MemberContactView;
import kr.co.deepple.member.application.dto.MemberInfoView;
import kr.co.deepple.member.application.dto.MemberProfileView;
import kr.co.deepple.member.application.dto.OtherMemberProfileView;
import kr.co.deepple.member.application.dto.ProfileAccessView;
import kr.co.deepple.member.application.dto.QInterviewInfoView;
import kr.co.deepple.member.application.dto.QInterviewResultView;
import kr.co.deepple.member.application.dto.QMemberContactView;
import kr.co.deepple.member.application.dto.QMemberInfoView;
import kr.co.deepple.member.application.dto.QMemberProfileView;
import kr.co.deepple.member.application.dto.QOtherMemberProfileView;
import kr.co.deepple.member.application.dto.QProfileAccessView;
import kr.co.deepple.member.application.port.in.MemberPreview;
import kr.co.deepple.member.domain.Hobby;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Optional<MemberInfoView> findInfoByMemberId(long memberId) {
    EnumPath<Hobby> hobby = enumPath(Hobby.class, "hobbyAlias");

    MemberInfoView memberInfoView =
        queryFactory
            .from(member)
            .leftJoin(member.profile.hobbies, hobby)
            .leftJoin(interviewAnswer)
            .on(member.id.eq(interviewAnswer.memberId))
            .leftJoin(interviewQuestion)
            .on(
                interviewQuestion
                    .id
                    .eq(interviewAnswer.questionId)
                    .and(interviewQuestion.isPublic.eq(true)))
            .where(member.id.eq(memberId))
            .transform(
                groupBy(member.id)
                    .as(
                        new QMemberInfoView(
                            member.id,
                            member.activityStatus.stringValue(),
                            member.isVip.isTrue(),
                            member.primaryContactType.stringValue(),
                            member.isDatingExamSubmitted.isTrue(),
                            member.profile.nickname.value.stringValue(),
                            member.profile.gender.stringValue(),
                            member.kakaoId.value,
                            member.profile.yearOfBirth.value,
                            member.profile.height,
                            member.phoneNumber.value,
                            member.profile.job.stringValue(),
                            member.profile.highestEducation.stringValue(),
                            member.profile.region.city.stringValue(),
                            member.profile.region.district.stringValue(),
                            member.profile.mbti.stringValue(),
                            member.profile.smokingStatus.stringValue(),
                            member.profile.drinkingStatus.stringValue(),
                            member.profile.religion.stringValue(),
                            set(hobby.stringValue()),
                            set(
                                (new QInterviewInfoView(
                                    interviewQuestion.id,
                                    interviewQuestion.content,
                                    interviewAnswer.content))))))
            .get(memberId);

    return Optional.ofNullable(memberInfoView);
  }

  public Optional<MemberProfileView> findProfileByMemberId(long memberId) {
    EnumPath<Hobby> hobby = enumPath(Hobby.class, "hobbyAlias");

    MemberProfileView memberProfileView =
        queryFactory
            .from(member)
            .leftJoin(member.profile.hobbies, hobby)
            .transform(
                groupBy(member.id)
                    .as(
                        new QMemberProfileView(
                            member.profile.nickname.value,
                            member.profile.yearOfBirth.value,
                            member.profile.gender.stringValue(),
                            member.profile.height,
                            member.profile.job.stringValue(),
                            set(hobby.stringValue()),
                            member.profile.mbti.stringValue(),
                            member.profile.region.city.stringValue(),
                            member.profile.region.district.stringValue(),
                            member.profile.smokingStatus.stringValue(),
                            member.profile.drinkingStatus.stringValue(),
                            member.profile.highestEducation.stringValue(),
                            member.profile.religion.stringValue())))
            .get(memberId);
    return Optional.ofNullable(memberProfileView);
  }

  public Optional<MemberContactView> findContactsByMemberId(long memberId) {
    MemberContactView memberContactView =
        queryFactory
            .select(
                new QMemberContactView(
                    member.phoneNumber.value,
                    member.kakaoId.value,
                    member.primaryContactType.stringValue()))
            .from(member)
            .where(member.id.eq(memberId))
            .fetchOne();

    return Optional.ofNullable(memberContactView);
  }

  public Optional<OtherMemberProfileView> findOtherProfileByMemberId(
      long memberId, long otherMemberId) {
    EnumPath<Hobby> hobby = enumPath(Hobby.class, "hobbyAlias");

    OtherMemberProfileView otherMemberProfileView =
        queryFactory
            .from(member)
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
            .on(
                memberIntroduction
                    .memberId
                    .eq(memberId)
                    .and(memberIntroduction.introducedMemberId.eq(otherMemberId)))
            .where(member.id.eq(otherMemberId))
            .transform(
                groupBy(member.id)
                    .as(
                        new QOtherMemberProfileView(
                            member.id,
                            member.profile.nickname.value,
                            profileImage.imageUrl.value,
                            member.profile.yearOfBirth.value,
                            member.profile.gender.stringValue(),
                            member.profile.height,
                            member.profile.job.stringValue(),
                            set(hobby.stringValue()),
                            member.profile.mbti.stringValue(),
                            member.profile.region.city.stringValue(),
                            member.profile.region.district.stringValue(),
                            member.profile.smokingStatus.stringValue(),
                            member.profile.drinkingStatus.stringValue(),
                            member.profile.highestEducation.stringValue(),
                            member.profile.religion.stringValue(),
                            like.level.stringValue(),
                            member.lastAccessedAt,
                            match.id,
                            match.requesterId,
                            match.responderId,
                            match.requestMessage.value,
                            match.responseMessage.value,
                            match.status.stringValue(),
                            match.requesterContactType.stringValue(),
                            match.responderContactType.stringValue(),
                            member.phoneNumber.value,
                            member.kakaoId.value,
                            profileExchange.id,
                            profileExchange.requesterId,
                            profileExchange.responderId,
                            profileExchange.status.stringValue(),
                            memberIntroduction.type.stringValue())))
            .get(otherMemberId);

    return Optional.ofNullable(otherMemberProfileView);
  }

  public Optional<ProfileAccessView> findProfileAccessViewByMemberId(
      long memberId, long otherMemberId) {
    ProfileAccessView view =
        queryFactory
            .select(
                new QProfileAccessView(
                    cases().when(memberIntroduction.id.isNotNull()).then(true).otherwise(false),
                    match.requesterId,
                    match.responderId,
                    profileExchange.requesterId,
                    profileExchange.responderId,
                    profileExchange.status.stringValue(),
                    cases().when(like.id.isNotNull()).then(true).otherwise(false),
                    block.id.isNotNull(),
                    member.activityStatus.stringValue()))
            .from(member)
            .leftJoin(memberIntroduction)
            .on(
                memberIntroduction
                    .introducedMemberId
                    .eq(otherMemberId)
                    .and(memberIntroduction.memberId.eq(memberId)))
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

  public List<InterviewResultView> findInterviewsByMemberId(long memberId) {
    return queryFactory
        .from(interviewQuestion)
        .innerJoin(interviewAnswer)
        .on(getInterviewAnswerJoinCondition(memberId))
        .select(
            new QInterviewResultView(
                interviewQuestion.content,
                interviewQuestion.category.stringValue(),
                interviewAnswer.content))
        .where(interviewQuestion.isPublic.eq(true))
        .fetch();
  }

  private BooleanExpression getProfileExchangeJoinCondition(Long memberId, Long otherMemberId) {
    return (profileExchange
            .requesterId
            .eq(memberId)
            .and(profileExchange.responderId.eq(otherMemberId)))
        .or(
            profileExchange
                .requesterId
                .eq(otherMemberId)
                .and(profileExchange.responderId.eq(memberId)));
  }

  private BooleanExpression getMatchJoinCondition(Long memberId, Long otherMemberId) {
    return (match
            .requesterId
            .eq(memberId)
            .and(match.responderId.eq(otherMemberId))
            .or(match.requesterId.eq(otherMemberId).and(match.responderId.eq(memberId))))
        .and(match.status.notIn(MatchStatus.REJECT_CHECKED, MatchStatus.EXPIRED));
  }

  private BooleanExpression getMatchJoinConditionForProfile(Long memberId, Long otherMemberId) {
    return match
        .requesterId
        .eq(otherMemberId)
        .and(match.responderId.eq(memberId))
        .and(match.status.notIn(MatchStatus.REJECT_CHECKED, MatchStatus.REJECTED));
  }

  private BooleanExpression getInterviewAnswerJoinCondition(Long memberId) {
    return interviewQuestion
        .id
        .eq(interviewAnswer.questionId)
        .and(interviewAnswer.memberId.eq(memberId));
  }

  public List<MemberPreview> findPreviewsByMemberIds(Collection<Long> memberIds) {
    return queryFactory
        .select(
            Projections.constructor(
                MemberPreview.class,
                member.id,
                member.profile.nickname.value,
                profileImage.imageUrl.value,
                member.profile.region.city.stringValue(),
                member.profile.yearOfBirth.value))
        .from(member)
        .leftJoin(profileImage)
        .on(profileImage.memberId.eq(member.id).and(profileImage.isPrimary.eq(true)))
        .where(member.id.in(memberIds))
        .fetch();
  }

  public Set<Long> findIdsByNicknameAndPhoneNumber(String nicknameKeyword, String phoneNumber) {
    return new HashSet<>(
        queryFactory
            .select(member.id)
            .from(member)
            .where(
                nicknameKeyword != null
                    ? member.profile.nickname.value.contains(nicknameKeyword)
                    : null,
                phoneNumber != null ? member.phoneNumber.value.eq(phoneNumber) : null)
            .fetch());
  }
}
