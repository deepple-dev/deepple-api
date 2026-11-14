package kr.co.deepple.community.adapter.out.persistence;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;
import static com.querydsl.core.types.dsl.Expressions.enumPath;
import static kr.co.deepple.block.domain.QBlock.block;
import static kr.co.deepple.community.domain.QProfileExchange.profileExchange;
import static kr.co.deepple.community.domain.QSelfIntroduction.selfIntroduction;
import static kr.co.deepple.datingexam.domain.QDatingExamSubmitResult.datingExamSubmitResult;
import static kr.co.deepple.like.domain.QLike.like;
import static kr.co.deepple.member.domain.QMember.member;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.community.application.dto.QSelfIntroductionSummaryView;
import kr.co.deepple.community.application.dto.QSelfIntroductionView;
import kr.co.deepple.community.application.dto.SelfIntroductionSearchCondition;
import kr.co.deepple.community.application.dto.SelfIntroductionSummaryView;
import kr.co.deepple.community.application.dto.SelfIntroductionView;
import kr.co.deepple.member.domain.Hobby;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SelfIntroductionQueryRepository {

  private static final int PAGE_SIZE = 10;
  private final JPAQueryFactory queryFactory;

  public List<SelfIntroductionSummaryView> findSelfIntroductions(
      SelfIntroductionSearchCondition searchCondition, Long lastId, long memberId) {
    Set<Long> blockedIds = getBlockedIds(memberId);

    BooleanExpression condition = getSearchCondition(searchCondition, lastId, memberId, blockedIds);

    return queryFactory
        .select(
            new QSelfIntroductionSummaryView(
                selfIntroduction.id,
                member.profile.nickname.value,
                member.profile.yearOfBirth.value,
                selfIntroduction.title,
                selfIntroduction.content,
                selfIntroduction.imageUrl,
                selfIntroduction.createdAt,
                datingExamSubmitResult.dominantPersonalityType.stringValue()))
        .from(selfIntroduction)
        .join(member)
        .on(member.id.eq(selfIntroduction.memberId))
        .leftJoin(datingExamSubmitResult)
        .on(datingExamSubmitResult.memberId.eq(member.id))
        .where(condition)
        .limit(PAGE_SIZE)
        .orderBy(selfIntroduction.id.desc())
        .fetch();
  }

  public List<SelfIntroductionSummaryView> findMySelfIntroductions(Long lastId, long memberId) {
    BooleanExpression condition =
        selfIntroduction.deletedAt.isNull().and(selfIntroduction.memberId.eq(memberId));
    if (lastId != null) {
      condition = condition.and(selfIntroduction.id.lt(lastId));
    }

    return queryFactory
        .select(
            new QSelfIntroductionSummaryView(
                selfIntroduction.id,
                member.profile.nickname.value,
                member.profile.yearOfBirth.value,
                selfIntroduction.title,
                selfIntroduction.content,
                selfIntroduction.imageUrl,
                selfIntroduction.createdAt,
                datingExamSubmitResult.dominantPersonalityType.stringValue()))
        .from(selfIntroduction)
        .join(member)
        .on(member.id.eq(selfIntroduction.memberId))
        .leftJoin(datingExamSubmitResult)
        .on(datingExamSubmitResult.memberId.eq(member.id))
        .where(condition)
        .limit(PAGE_SIZE)
        .orderBy(selfIntroduction.id.desc())
        .fetch();
  }

  public Optional<SelfIntroductionView> findSelfIntroductionByIdWithMemberId(
      long id, long memberId) {
    EnumPath<Hobby> hobby = enumPath(Hobby.class, "hobbyAlias");

    Map<Long, SelfIntroductionView> view =
        queryFactory
            .from(selfIntroduction)
            .leftJoin(member)
            .on(member.id.eq(selfIntroduction.memberId))
            .leftJoin(like)
            .on(like.senderId.eq(memberId).and(like.receiverId.eq(member.id)))
            .leftJoin(member.profile.hobbies, hobby)
            .leftJoin(profileExchange)
            .on(getProfileExchangeJoinCondition(memberId))
            .leftJoin(datingExamSubmitResult)
            .on(datingExamSubmitResult.memberId.eq(member.id))
            .where(selfIntroduction.id.eq(id).and(selfIntroduction.deletedAt.isNull()))
            .transform(
                groupBy(member.id)
                    .as(
                        new QSelfIntroductionView(
                            member.id,
                            member.profile.nickname.value,
                            member.profile.yearOfBirth.value,
                            member.profile.region.city.stringValue(),
                            member.profile.region.district.stringValue(),
                            member.profile.mbti.stringValue(),
                            set(hobby.stringValue()),
                            member.profile.gender.stringValue(),
                            datingExamSubmitResult.dominantPersonalityType.stringValue(),
                            like.level.stringValue(),
                            selfIntroduction.title,
                            selfIntroduction.content,
                            selfIntroduction.imageUrl,
                            profileExchange.status.stringValue(),
                            selfIntroduction.createdAt)));

    return view.values().stream().findFirst();
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

  private BooleanExpression getProfileExchangeJoinCondition(Long memberId) {
    return (profileExchange.requesterId.eq(member.id).and(profileExchange.responderId.eq(memberId)))
        .or(
            profileExchange
                .requesterId
                .eq(memberId)
                .and(profileExchange.responderId.eq(member.id)));
  }

  private BooleanExpression getSearchCondition(
      SelfIntroductionSearchCondition searchCondition,
      Long lastId,
      Long memberId,
      Set<Long> blockedIds) {
    BooleanExpression condition =
        selfIntroduction
            .deletedAt
            .isNull()
            .and(selfIntroduction.isOpened.eq(true).or(selfIntroduction.memberId.eq(memberId)));
    if (lastId != null) {
      condition = condition.and(selfIntroduction.id.lt(lastId));
    }
    if (!blockedIds.isEmpty()) {
      condition = condition.and(member.id.notIn(blockedIds));
    }

    condition = addYearOfBirthCondition(condition, searchCondition);
    condition = addGenderCondition(condition, searchCondition);
    condition = addPreferredCityCondition(condition, searchCondition);
    return condition == null ? member.isNotNull() : condition;
  }

  private BooleanExpression addYearOfBirthCondition(
      BooleanExpression condition, SelfIntroductionSearchCondition searchCondition) {
    if (searchCondition == null) {
      return condition;
    }

    if (searchCondition.fromYearOfBirth() != null && searchCondition.toYearOfBirth() != null) {
      condition =
          (condition == null)
              ? member.profile.yearOfBirth.value.between(
                  searchCondition.fromYearOfBirth(), searchCondition.toYearOfBirth())
              : condition.and(
                  member.profile.yearOfBirth.value.between(
                      searchCondition.fromYearOfBirth(), searchCondition.toYearOfBirth()));
    } else if (searchCondition.fromYearOfBirth() != null) {
      condition =
          (condition == null)
              ? member.profile.yearOfBirth.value.goe(searchCondition.fromYearOfBirth())
              : condition.and(
                  member.profile.yearOfBirth.value.goe(searchCondition.fromYearOfBirth()));
    } else if (searchCondition.toYearOfBirth() != null) {
      condition =
          (condition == null)
              ? member.profile.yearOfBirth.value.loe(searchCondition.toYearOfBirth())
              : condition.and(
                  member.profile.yearOfBirth.value.loe(searchCondition.toYearOfBirth()));
    }
    return condition;
  }

  private BooleanExpression addGenderCondition(
      BooleanExpression condition, SelfIntroductionSearchCondition searchCondition) {
    if (searchCondition == null) {
      return condition;
    }

    if (searchCondition.gender() != null) {
      condition =
          (condition == null)
              ? member.profile.gender.eq(searchCondition.gender())
              : condition.and(member.profile.gender.eq(searchCondition.gender()));
    }
    return condition;
  }

  private BooleanExpression addPreferredCityCondition(
      BooleanExpression condition, SelfIntroductionSearchCondition searchCondition) {
    if (searchCondition == null) {
      return condition;
    }

    if (searchCondition.preferredCities() != null && !searchCondition.preferredCities().isEmpty()) {
      condition =
          (condition == null)
              ? member.profile.region.city.stringValue().in(searchCondition.preferredCities())
              : condition.and(
                  member.profile.region.city.stringValue().in(searchCondition.preferredCities()));
    }
    return condition;
  }
}
