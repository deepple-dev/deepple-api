package kr.co.deepple.recommendation.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.like.command.domain.Like;
import kr.co.deepple.like.command.domain.LikeLevel;
import kr.co.deepple.like.fixture.LikeFixture;
import kr.co.deepple.member.domain.District;
import kr.co.deepple.member.domain.DrinkingStatus;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.domain.Grade;
import kr.co.deepple.member.domain.Hobby;
import kr.co.deepple.member.domain.Mbti;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.ProfileImage;
import kr.co.deepple.member.domain.Religion;
import kr.co.deepple.member.domain.SmokingStatus;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.member.fixture.ProfileImageFixture;
import kr.co.deepple.recommendation.application.dto.IntroductionSearchCondition;
import kr.co.deepple.recommendation.application.dto.MemberIntroductionProfileQueryResult;
import kr.co.deepple.recommendation.domain.IntroductionType;
import kr.co.deepple.recommendation.domain.MemberIntroduction;
import kr.co.deepple.recommendation.fixture.MemberIntroductionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class IntroductionQueryRepositoryTest extends RepositoryTestSupport {

  @Autowired private TestEntityManager entityManager;

  @Autowired private IntroductionQueryRepository introductionQueryRepository;

  @Nested
  @DisplayName("findAllIntroductionMemberId 메서드 테스트")
  class FindAllIntroductionMemberIdTest {

    @ParameterizedTest
    @ValueSource(
        strings = {
          "excludedIds",
          "minAge",
          "maxAge",
          "hobbies",
          "religion",
          "cities",
          "smokingStatus",
          "drinkingStatus",
          "memberGrade",
          "gender",
          "joinedAfter",
          "isProfilePublic",
          "null"
        })
    @DisplayName("search condition에 대한 검증")
    void findIntroductionMemberIdsWhenSuccess(String fieldName) {
      // given
      int currentYear = Calendar.getInstance().get(Calendar.YEAR);
      int limit = 10;

      Hobby hobby1 = Hobby.ANIMATION;
      Hobby hobby2 = Hobby.BOARD_GAMES;
      Hobby hobby3 = Hobby.CAMPING;
      Hobby hobby4 = Hobby.BADMINTON_AND_TENNIS;

      Member member1 =
          MemberFixture.builder()
              .yearOfBirth(currentYear - 19) // 20살
              .hobbies(Set.of(hobby1, hobby2))
              .religion(Religion.BUDDHIST)
              .district(District.DONG_GU_DAEJEON)
              .smokingStatus(SmokingStatus.DAILY)
              .drinkingStatus(DrinkingStatus.SOCIAL)
              .gender(Gender.MALE)
              .profilePublic(true)
              .build();
      entityManager.persist(member1);
      entityManager.flush();

      Member member2 =
          MemberFixture.builder()
              .yearOfBirth(currentYear - 39) // 40살
              .hobbies(Set.of(hobby3, hobby4))
              .religion(Religion.NONE)
              .district(District.GANGNAM_GU)
              .smokingStatus(SmokingStatus.NONE)
              .drinkingStatus(DrinkingStatus.NONE)
              .gender(Gender.FEMALE)
              .profilePublic(!fieldName.equals("isProfilePublic"))
              .build();
      entityManager.persist(member2);
      entityManager.flush();

      IntroductionSearchCondition condition = mock(IntroductionSearchCondition.class);
      when(condition.getExcludedMemberIds())
          .thenReturn(fieldName.equals("excludedIds") ? Set.of(member2.getId()) : Set.of());

      // 수정 by 공태현 (출생연도에 따른 계산으로.)
      when(condition.getMaxAge())
          .thenReturn(
              fieldName.equals("maxAge")
                  ? currentYear - member1.getProfile().getYearOfBirth().getValue() + 1
                  : null); // 나이 최대 20살
      when(condition.getMinAge())
          .thenReturn(
              fieldName.equals("minAge")
                  ? currentYear - member2.getProfile().getYearOfBirth().getValue() + 1
                  : null); // 나이 최소 40살

      when(condition.getHobbies())
          .thenReturn(
              fieldName.equals("hobbies")
                  ? member1.getProfile().getHobbies().stream()
                      .map(Hobby::name)
                      .collect(Collectors.toSet())
                  : Set.of());
      when(condition.getReligion())
          .thenReturn(
              fieldName.equals("religion") ? member1.getProfile().getReligion().name() : null);
      when(condition.getCities())
          .thenReturn(
              fieldName.equals("cities")
                  ? Set.of(member1.getProfile().getRegion().getCity().name())
                  : Set.of());
      when(condition.getSmokingStatus())
          .thenReturn(
              fieldName.equals("smokingStatus")
                  ? member1.getProfile().getSmokingStatus().name()
                  : null);
      when(condition.getDrinkingStatus())
          .thenReturn(
              fieldName.equals("drinkingStatus")
                  ? member1.getProfile().getDrinkingStatus().name()
                  : null);
      when(condition.getMemberGrade())
          .thenReturn(fieldName.equals("memberGrade") ? Grade.DIAMOND.name() : null);
      when(condition.getGender())
          .thenReturn(fieldName.equals("gender") ? member1.getProfile().getGender().name() : null);
      when(condition.getJoinedAfter())
          .thenReturn(fieldName.equals("joinedAfter") ? LocalDateTime.now().plusDays(1) : null);

      // when
      Set<Long> result = introductionQueryRepository.findAllIntroductionMemberId(condition, limit);

      // then
      switch (fieldName) {
        case "excludedIds" -> assertThat(result).containsExactly(member1.getId());
        case "minAge" -> assertThat(result).containsExactly(member2.getId());
        case "maxAge" -> assertThat(result).containsExactly(member1.getId());
        case "hobbies" -> assertThat(result).containsExactly(member1.getId());
        case "religion" -> assertThat(result).containsExactly(member1.getId());
        case "cities" -> assertThat(result).containsExactly(member1.getId());
        case "smokingStatus" -> assertThat(result).containsExactly(member1.getId());
        case "drinkingStatus" -> assertThat(result).containsExactly(member1.getId());
        case "memberGrade" -> assertThat(result).isEmpty();
        case "gender" -> assertThat(result).containsExactly(member1.getId());
        case "joinedAfter" -> assertThat(result).isEmpty();
        case "isProfilePublic" -> assertThat(result).containsExactly(member1.getId());
        default -> assertThat(result).containsExactlyInAnyOrder(member1.getId(), member2.getId());
      }
    }
  }

  @Nested
  @DisplayName("findAllMemberIntroductionProfileQueryResultByMemberIds 메서드 테스트")
  class FindAllMemberIntroductionProfileQueryResultByMemberIdsIdsTest {

    @ParameterizedTest
    @ValueSource(strings = {"introduced", "notIntroduced1", "notIntroduced2"})
    @DisplayName("멤버 ID 목록에 해당하는 회원의 소개 프로필 리턴")
    void findAllMemberIntroductionProfileQueryResultByMemberIdsWhenSuccess(String fieldName) {
      // given
      Hobby hobby1 = Hobby.ANIMATION;
      Hobby hobby2 = Hobby.BOARD_GAMES;
      Hobby hobby3 = Hobby.CAMPING;
      Hobby hobby4 = Hobby.BADMINTON_AND_TENNIS;

      Member me =
          MemberFixture.builder()
              .yearOfBirth(Calendar.getInstance().get(Calendar.YEAR) - 25) // 26살
              .hobbies(Set.of(hobby1, hobby2))
              .religion(Religion.BUDDHIST)
              .district(District.DONG_GU_DAEJEON)
              .smokingStatus(SmokingStatus.DAILY)
              .drinkingStatus(DrinkingStatus.SOCIAL)
              .build();
      entityManager.persist(me);
      entityManager.flush();

      Member introductionTargetMember =
          MemberFixture.builder()
              .yearOfBirth(Calendar.getInstance().get(Calendar.YEAR) - 25) // 26살
              .hobbies(Set.of(hobby3, hobby4))
              .religion(Religion.NONE)
              .district(District.GANGBUK_GU)
              .smokingStatus(SmokingStatus.NONE)
              .drinkingStatus(DrinkingStatus.NONE)
              .mbti(Mbti.ISTP)
              .build();
      entityManager.persist(introductionTargetMember);
      entityManager.flush();

      ProfileImage primaryProfileImage =
          ProfileImageFixture.builder()
              .memberId(introductionTargetMember.getId())
              .imageUrl("https://example.com")
              .primary(true)
              .order(1)
              .build();

      ProfileImage nonPrimaryProfileImage =
          ProfileImageFixture.builder()
              .memberId(introductionTargetMember.getId())
              .imageUrl("https://example2.com")
              .primary(false)
              .order(2)
              .build();

      entityManager.persist(primaryProfileImage);
      entityManager.persist(nonPrimaryProfileImage);

      Like like =
          LikeFixture.builder()
              .senderId(me.getId())
              .receiverId(introductionTargetMember.getId())
              .level(LikeLevel.INTERESTED)
              .build();
      entityManager.persist(like);
      entityManager.flush();

      IntroductionType type = IntroductionType.DIAMOND_GRADE;

      if (fieldName.equals("introduced")) {
        MemberIntroduction memberIntroduction =
            MemberIntroductionFixture.builder()
                .memberId(me.getId())
                .introducedMemberId(introductionTargetMember.getId())
                .type(type)
                .build();
        entityManager.persist(memberIntroduction);
        entityManager.flush();
      }
      if (fieldName.equals("notIntroduced1")) {
        MemberIntroduction memberIntroduction =
            MemberIntroductionFixture.builder()
                .memberId(introductionTargetMember.getId())
                .introducedMemberId(me.getId())
                .type(type)
                .build();
        entityManager.persist(memberIntroduction);
        entityManager.flush();
      }
      if (fieldName.equals("notIntroduced2")) {
        // do nothing
      }

      // when
      List<MemberIntroductionProfileQueryResult> result =
          introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(
              me.getId(), Set.of(introductionTargetMember.getId()));

      // then
      assertThat(result).hasSize(1);
      MemberIntroductionProfileQueryResult memberIntroductionProfileQueryResult = result.getFirst();
      assertThat(memberIntroductionProfileQueryResult.memberId())
          .isEqualTo(introductionTargetMember.getId());
      assertThat(memberIntroductionProfileQueryResult.profileImageUrl())
          .isEqualTo(primaryProfileImage.getUrl());
      assertThat(memberIntroductionProfileQueryResult.hobbies())
          .containsExactlyInAnyOrder(hobby3.name(), hobby4.name());
      assertThat(memberIntroductionProfileQueryResult.religion())
          .isEqualTo(introductionTargetMember.getProfile().getReligion().name());
      assertThat(memberIntroductionProfileQueryResult.mbti())
          .isEqualTo(introductionTargetMember.getProfile().getMbti().name());
      assertThat(memberIntroductionProfileQueryResult.likeLevel())
          .isEqualTo(like.getLevel().name());
      if (fieldName.equals("introduced")) {
        assertThat(memberIntroductionProfileQueryResult.isIntroduced()).isTrue();
      } else {
        assertThat(memberIntroductionProfileQueryResult.isIntroduced()).isFalse();
      }
    }

    @Test
    @DisplayName("소개되지 않은 멤버는 isIntroduced가 false로 설정되어야 한다.")
    void isIntroducedFalseWhenNotIntroduced() {
      // given
      Hobby hobby1 = Hobby.ANIMATION;
      Hobby hobby2 = Hobby.BOARD_GAMES;
      Hobby hobby3 = Hobby.CAMPING;
      Hobby hobby4 = Hobby.BADMINTON_AND_TENNIS;

      Member me =
          MemberFixture.builder()
              .yearOfBirth(Calendar.getInstance().get(Calendar.YEAR) - 25) // 26살
              .hobbies(Set.of(hobby1, hobby2))
              .religion(Religion.BUDDHIST)
              .district(District.DONG_GU_DAEJEON)
              .smokingStatus(SmokingStatus.DAILY)
              .drinkingStatus(DrinkingStatus.SOCIAL)
              .build();
      entityManager.persist(me);
      entityManager.flush();

      Member isIntroducedTrueMember =
          MemberFixture.builder()
              .yearOfBirth(Calendar.getInstance().get(Calendar.YEAR) - 25) // 26살
              .hobbies(Set.of(hobby3, hobby4))
              .religion(Religion.NONE)
              .district(District.GANGBUK_GU)
              .smokingStatus(SmokingStatus.NONE)
              .drinkingStatus(DrinkingStatus.NONE)
              .mbti(Mbti.ISTP)
              .build();
      entityManager.persist(isIntroducedTrueMember);
      entityManager.flush();

      Member isIntroducedFalseMember =
          MemberFixture.builder()
              .yearOfBirth(Calendar.getInstance().get(Calendar.YEAR) - 25) // 26살
              .hobbies(Set.of(hobby3, hobby4))
              .religion(Religion.NONE)
              .district(District.GANGBUK_GU)
              .smokingStatus(SmokingStatus.NONE)
              .drinkingStatus(DrinkingStatus.NONE)
              .mbti(Mbti.ISTP)
              .build();
      entityManager.persist(isIntroducedFalseMember);
      entityManager.flush();

      IntroductionType type = IntroductionType.DIAMOND_GRADE;

      MemberIntroduction memberIntroduction =
          MemberIntroductionFixture.builder()
              .memberId(me.getId())
              .introducedMemberId(isIntroducedTrueMember.getId())
              .type(type)
              .build();
      entityManager.persist(memberIntroduction);
      entityManager.flush();

      // when
      List<MemberIntroductionProfileQueryResult> result =
          introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(
              me.getId(), Set.of(isIntroducedTrueMember.getId(), isIntroducedFalseMember.getId()));

      // then
      assertThat(result).hasSize(2);

      MemberIntroductionProfileQueryResult isIntroducedTrueResult =
          result.getFirst().memberId() == isIntroducedTrueMember.getId()
              ? result.getFirst()
              : result.get(1);
      assertThat(isIntroducedTrueResult.isIntroduced()).isTrue();

      MemberIntroductionProfileQueryResult isIntroducedFalseResult =
          result.getFirst().memberId() == isIntroducedFalseMember.getId()
              ? result.getFirst()
              : result.get(1);
      assertThat(isIntroducedFalseResult.isIntroduced()).isFalse();
    }
  }
}
