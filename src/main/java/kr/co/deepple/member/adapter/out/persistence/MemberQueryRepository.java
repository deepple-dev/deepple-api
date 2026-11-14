package kr.co.deepple.member.adapter.out.persistence;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;
import static com.querydsl.core.types.dsl.Expressions.enumPath;
import static kr.co.deepple.member.domain.QMember.member;
import static kr.co.deepple.member.domain.QProfileImage.profileImage;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.member.application.dto.MemberContactView;
import kr.co.deepple.member.application.dto.MemberProfileView;
import kr.co.deepple.member.application.dto.QMemberContactView;
import kr.co.deepple.member.application.dto.QMemberProfileView;
import kr.co.deepple.member.application.port.in.MemberDetail;
import kr.co.deepple.member.application.port.in.MemberIdSearchCondition;
import kr.co.deepple.member.application.port.in.MemberPageItem;
import kr.co.deepple.member.application.port.in.MemberPageSearchCondition;
import kr.co.deepple.member.application.port.in.MemberPreview;
import kr.co.deepple.member.application.port.in.MemberProfileSummary;
import kr.co.deepple.member.application.port.in.MemberScreeningProfile;
import kr.co.deepple.member.application.port.in.MemberSearchCondition;
import kr.co.deepple.member.domain.ActivityStatus;
import kr.co.deepple.member.domain.AgeConverter;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.domain.Hobby;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

  private final JPAQueryFactory queryFactory;

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

  public Optional<MemberDetail> findDetailByMemberId(long memberId) {
    EnumPath<Hobby> hobby = enumPath(Hobby.class, "hobbyAlias");

    MemberDetail memberDetail =
        queryFactory
            .from(member)
            .leftJoin(member.profile.hobbies, hobby)
            .where(member.id.eq(memberId))
            .transform(
                groupBy(member.id)
                    .as(
                        Projections.constructor(
                            MemberDetail.class,
                            member.id,
                            member.activityStatus.stringValue(),
                            member.grade.stringValue(),
                            member.isVip,
                            member.isProfilePublic,
                            member.isDatingExamSubmitted,
                            member.primaryContactType.stringValue(),
                            member.phoneNumber.value,
                            member.kakaoId.value,
                            member.profile.nickname.value,
                            member.profile.gender.stringValue(),
                            member.profile.yearOfBirth.value,
                            member.profile.height,
                            member.profile.job.stringValue(),
                            member.profile.highestEducation.stringValue(),
                            member.profile.region.city.stringValue(),
                            member.profile.region.district.stringValue(),
                            member.profile.mbti.stringValue(),
                            member.profile.smokingStatus.stringValue(),
                            member.profile.drinkingStatus.stringValue(),
                            member.profile.religion.stringValue(),
                            set(hobby.stringValue()),
                            member.lastAccessedAt,
                            member.createdAt,
                            member.deletedAt)))
            .get(memberId);

    return Optional.ofNullable(memberDetail);
  }

  public Page<MemberPageItem> findPage(MemberPageSearchCondition condition, Pageable pageable) {
    BooleanExpression filter =
        Expressions.allOf(
            gradeEq(condition.grade()),
            genderEq(condition.gender()),
            condition.activityStatus() != null
                ? member.activityStatus.stringValue().eq(condition.activityStatus())
                : null,
            condition.nicknameKeyword() != null
                ? member.profile.nickname.value.contains(condition.nicknameKeyword())
                : null,
            condition.phoneNumber() != null
                ? member.phoneNumber.value.eq(condition.phoneNumber())
                : null,
            condition.joinedFrom() != null
                ? member.createdAt.goe(condition.joinedFrom().atStartOfDay())
                : null,
            condition.joinedTo() != null
                ? member.createdAt.lt(condition.joinedTo().plusDays(1).atStartOfDay())
                : null);

    List<MemberPageItem> items =
        queryFactory
            .select(
                Projections.constructor(
                    MemberPageItem.class,
                    member.id,
                    member.profile.nickname.value,
                    member.profile.gender.stringValue(),
                    member.activityStatus.stringValue(),
                    member.createdAt))
            .from(member)
            .where(filter)
            .orderBy(member.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    long totalCount = queryFactory.select(member.count()).from(member).where(filter).fetchOne();

    return new PageImpl<>(items, pageable, totalCount);
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
                member.profile.yearOfBirth.value,
                member.profile.gender.stringValue()))
        .from(member)
        .leftJoin(profileImage)
        .on(profileImage.memberId.eq(member.id).and(profileImage.isPrimary.eq(true)))
        .where(member.id.in(memberIds))
        .fetch();
  }

  public List<MemberProfileSummary> findProfilesByMemberIds(Collection<Long> memberIds) {
    EnumPath<Hobby> hobby = enumPath(Hobby.class, "hobbyAlias");
    StringExpression gender = member.profile.gender.stringValue();
    StringExpression city = member.profile.region.city.stringValue();
    StringExpression district = member.profile.region.district.stringValue();
    StringExpression mbti = member.profile.mbti.stringValue();
    StringExpression religion = member.profile.religion.stringValue();
    StringExpression hobbyName = hobby.stringValue();

    return queryFactory
        .from(member)
        .leftJoin(profileImage)
        .on(profileImage.memberId.eq(member.id).and(profileImage.isPrimary.eq(true)))
        .leftJoin(member.profile.hobbies, hobby)
        .where(member.id.in(memberIds))
        .transform(
            groupBy(member.id)
                .as(
                    member.profile.nickname.value,
                    profileImage.imageUrl.value,
                    member.profile.yearOfBirth.value,
                    gender,
                    city,
                    district,
                    mbti,
                    religion,
                    set(hobbyName)))
        .entrySet()
        .stream()
        .map(entry ->
            new MemberProfileSummary(
                entry.getKey(),
                entry.getValue().getOne(member.profile.nickname.value),
                entry.getValue().getOne(profileImage.imageUrl.value),
                AgeConverter.toAge(entry.getValue().getOne(member.profile.yearOfBirth.value)),
                entry.getValue().getOne(gender),
                entry.getValue().getOne(city),
                entry.getValue().getOne(district),
                entry.getValue().getOne(mbti),
                entry.getValue().getOne(religion),
                entry.getValue().getSet(hobbyName)))
        .toList();
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

  public Set<Long> searchIds(MemberIdSearchCondition condition) {
    return new HashSet<>(
        queryFactory
            .select(member.id)
            .from(member)
            .where(
                condition.nickname() != null
                    ? member.profile.nickname.value.eq(condition.nickname())
                    : null,
                condition.phoneNumber() != null
                    ? member.phoneNumber.value.eq(condition.phoneNumber())
                    : null,
                condition.joinedFrom() != null
                    ? member.createdAt.goe(condition.joinedFrom())
                    : null,
                condition.joinedTo() != null ? member.createdAt.loe(condition.joinedTo()) : null)
            .fetch());
  }

  public List<MemberScreeningProfile> findScreeningProfilesByMemberIds(Collection<Long> memberIds) {
    return queryFactory
        .select(
            Projections.constructor(
                MemberScreeningProfile.class,
                member.id,
                member.profile.nickname.value,
                member.profile.yearOfBirth.value,
                member.profile.gender.stringValue(),
                member.createdAt))
        .from(member)
        .where(member.id.in(memberIds))
        .fetch();
  }

  public List<Long> findIntroducibleIds(
      Collection<Long> candidateIds, Gender gender, long excludedMemberId) {
    return queryFactory
        .select(member.id)
        .from(member)
        .where(
            member.id.in(candidateIds),
            member.profile.gender.eq(gender),
            member.isProfilePublic.isTrue(),
            member.activityStatus.eq(ActivityStatus.ACTIVE),
            member.id.ne(excludedMemberId))
        .orderBy(member.id.desc())
        .fetch();
  }

  public Set<Long> searchIntroducibleIds(MemberSearchCondition condition, long limit) {
    JPAQuery<Long> query =
        queryFactory
            .select(member.id)
            .from(member)
            .where(
                idNotIn(condition.excludedMemberIds()),
                ageBetween(condition.minAge(), condition.maxAge()),
                cityIn(condition.cities()),
                religionEq(condition.religion()),
                smokingStatusEq(condition.smokingStatus()),
                drinkingStatusEq(condition.drinkingStatus()),
                gradeEq(condition.grade()),
                genderEq(condition.gender()),
                createdAtGoe(condition.joinedAfter()),
                member.activityStatus.eq(ActivityStatus.ACTIVE),
                member.isProfilePublic.isTrue())
            .orderBy(member.id.desc())
            .limit(limit);

    if (condition.hobbies() != null && !condition.hobbies().isEmpty()) {
      EnumPath<Hobby> hobby = enumPath(Hobby.class, "hobbyAlias");
      query
          .join(member.profile.hobbies, hobby)
          .where(hobby.stringValue().in(condition.hobbies()))
          .distinct();
    }
    return new HashSet<>(query.fetch());
  }

  private BooleanExpression idNotIn(Set<Long> memberIds) {
    if (memberIds == null || memberIds.isEmpty()) {
      return null;
    }
    return member.id.notIn(memberIds);
  }

  private BooleanExpression ageBetween(Integer minAge, Integer maxAge) {
    if (minAge == null && maxAge == null) {
      return null;
    }
    if (minAge == null) {
      return member.profile.yearOfBirth.value.goe(AgeConverter.toYearOfBirth(maxAge));
    }
    if (maxAge == null) {
      return member.profile.yearOfBirth.value.loe(AgeConverter.toYearOfBirth(minAge));
    }
    return member.profile.yearOfBirth.value.between(
        AgeConverter.toYearOfBirth(maxAge), AgeConverter.toYearOfBirth(minAge));
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
}
