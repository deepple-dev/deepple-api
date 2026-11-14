package kr.co.deepple.member.adapter.out.persistence;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;
import static com.querydsl.core.types.dsl.Expressions.constant;
import static com.querydsl.core.types.dsl.Expressions.enumPath;
import static kr.co.deepple.admin.domain.QWarning.warning;
import static kr.co.deepple.interview.domain.QInterviewAnswer.interviewAnswer;
import static kr.co.deepple.interview.domain.QInterviewQuestion.interviewQuestion;
import static kr.co.deepple.member.domain.QMember.member;
import static kr.co.deepple.member.domain.QProfileImage.profileImage;
import static kr.co.deepple.notification.domain.QNotificationPreference.notificationPreference;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import kr.co.deepple.member.application.dto.AdminMemberDetailView;
import kr.co.deepple.member.application.dto.AdminMemberSearchCondition;
import kr.co.deepple.member.application.dto.AdminMemberView;
import kr.co.deepple.member.application.dto.HeartBalanceView;
import kr.co.deepple.member.application.dto.InterviewInfoView;
import kr.co.deepple.member.application.dto.QAdminMemberDetailView;
import kr.co.deepple.member.application.dto.QAdminMemberSettingInfo;
import kr.co.deepple.member.application.dto.QAdminMemberStatusInfo;
import kr.co.deepple.member.application.dto.QAdminMemberView;
import kr.co.deepple.member.application.dto.QInterviewInfoView;
import kr.co.deepple.member.application.dto.QProfileInfo;
import kr.co.deepple.member.domain.Hobby;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminMemberQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Page<AdminMemberView> findMembers(
      AdminMemberSearchCondition condition, Pageable pageable) {
    List<AdminMemberView> views =
        queryFactory
            .select(
                new QAdminMemberView(
                    member.id,
                    member.profile.nickname.value,
                    member.profile.gender.stringValue(),
                    member.activityStatus.stringValue(),
                    member.createdAt,
                    warning.count().intValue()))
            .from(member)
            .leftJoin(warning)
            .on(warning.memberId.eq(member.id))
            .where(buildFindMembersFilterExpression(condition))
            .groupBy(member.id)
            .orderBy(member.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    long totalCount =
        queryFactory
            .select(member.count())
            .from(member)
            .where(buildFindMembersFilterExpression(condition))
            .fetchOne();

    return new PageImpl<>(views, pageable, totalCount);
  }

  private BooleanExpression buildFindMembersFilterExpression(AdminMemberSearchCondition condition) {
    return Expressions.allOf(
        activityStatusEq(condition.activityStatus()),
        genderEq(condition.gender()),
        gradeEq(condition.grade()),
        nicknameContains(condition.nickname()),
        phoneNumberEq(condition.phoneNumber()),
        createdAtGoe(condition.startDate()),
        createdAtLoe(condition.endDate()));
  }

  private BooleanExpression activityStatusEq(String activityStatus) {
    return activityStatus != null ? member.activityStatus.stringValue().eq(activityStatus) : null;
  }

  private BooleanExpression genderEq(String gender) {
    return gender != null ? member.profile.gender.stringValue().eq(gender) : null;
  }

  private BooleanExpression gradeEq(String grade) {
    return grade != null ? member.grade.stringValue().eq(grade) : null;
  }

  private BooleanExpression nicknameContains(String nickname) {
    return nickname != null ? member.profile.nickname.value.contains(nickname) : null;
  }

  private BooleanExpression phoneNumberEq(String phoneNumber) {
    return phoneNumber != null ? member.phoneNumber.value.eq(phoneNumber) : null;
  }

  private BooleanExpression createdAtGoe(LocalDate createdDateGoe) {
    return createdDateGoe != null ? member.createdAt.goe(createdDateGoe.atStartOfDay()) : null;
  }

  private BooleanExpression createdAtLoe(LocalDate createdDateLoe) {
    return createdDateLoe != null
        ? member.createdAt.lt(createdDateLoe.plusDays(1).atStartOfDay())
        : null;
  }

  public Optional<AdminMemberDetailView> findById(long memberId) {
    List<InterviewInfoView> interviewInfos =
        queryFactory
            .select(
                new QInterviewInfoView(
                    interviewQuestion.id, interviewQuestion.content, interviewAnswer.content))
            .from(interviewAnswer)
            .leftJoin(interviewQuestion)
            .on(interviewAnswer.questionId.eq(interviewQuestion.id))
            .where(interviewAnswer.memberId.eq(memberId))
            .fetch();

    boolean hasInterviewAnswers = !interviewInfos.isEmpty();

    int warningCount =
        queryFactory
            .select(warning.count())
            .from(warning)
            .where(warning.memberId.eq(memberId))
            .fetchOne()
            .intValue();

    List<String> profileImageUrls =
        queryFactory
            .select(profileImage.imageUrl.value)
            .from(profileImage)
            .where(profileImage.memberId.eq(memberId))
            .orderBy(profileImage.order.asc())
            .fetch();

    EnumPath<Hobby> hobby = enumPath(Hobby.class, "hobbyAlias");

    return Optional.ofNullable(
        queryFactory
            .from(member)
            .leftJoin(notificationPreference)
            .on(notificationPreference.memberId.eq(member.id))
            .leftJoin(member.profile.hobbies, hobby)
            .where(member.id.eq(memberId))
            .transform(
                groupBy(member.id)
                    .as(
                        new QAdminMemberDetailView(
                            new QAdminMemberSettingInfo(
                                member.grade.stringValue(),
                                member.isProfilePublic,
                                member.activityStatus.stringValue(),
                                member.isVip,
                                notificationPreference.isEnabledGlobally),
                            new QAdminMemberStatusInfo(
                                member.primaryContactType.stringValue(),
                                constant(hasInterviewAnswers),
                                constant(warningCount),
                                member.isDatingExamSubmitted.isTrue()),
                            member.profile.nickname.value,
                            member.profile.gender.stringValue(),
                            member.kakaoId.value,
                            member.profile.yearOfBirth.value,
                            member.profile.height.intValue(),
                            member.phoneNumber.value,
                            constant(new HeartBalanceView(0L, 0L, 0L)),
                            constant(profileImageUrls),
                            new QProfileInfo(
                                member.profile.job.stringValue(),
                                member.profile.highestEducation.stringValue(),
                                member.profile.region.city.stringValue(),
                                member.profile.region.district.stringValue(),
                                member.profile.mbti.stringValue(),
                                member.profile.smokingStatus.stringValue(),
                                member.profile.drinkingStatus.stringValue(),
                                member.profile.religion.stringValue(),
                                set(hobby.stringValue())),
                            constant(interviewInfos),
                            member.createdAt,
                            member.deletedAt)))
            .get(memberId));
  }
}
