package kr.co.deepple.member.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.member.application.dto.MemberContactView;
import kr.co.deepple.member.application.dto.MemberProfileView;
import kr.co.deepple.member.application.port.in.MemberDetail;
import kr.co.deepple.member.application.port.in.MemberIdSearchCondition;
import kr.co.deepple.member.application.port.in.MemberPageItem;
import kr.co.deepple.member.application.port.in.MemberPageSearchCondition;
import kr.co.deepple.member.application.port.in.MemberPreview;
import kr.co.deepple.member.application.port.in.MemberScreeningProfile;
import kr.co.deepple.member.application.port.in.MemberSearchCondition;
import kr.co.deepple.member.domain.ActivityStatus;
import kr.co.deepple.member.domain.AgeConverter;
import kr.co.deepple.member.domain.District;
import kr.co.deepple.member.domain.DrinkingStatus;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.domain.Grade;
import kr.co.deepple.member.domain.HighestEducation;
import kr.co.deepple.member.domain.Hobby;
import kr.co.deepple.member.domain.Job;
import kr.co.deepple.member.domain.Mbti;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.MemberProfile;
import kr.co.deepple.member.domain.PrimaryContactType;
import kr.co.deepple.member.domain.Religion;
import kr.co.deepple.member.domain.SmokingStatus;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.member.fixture.ProfileImageFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class MemberQueryRepositoryTest extends RepositoryTestSupport {

  @Autowired private MemberQueryRepository memberQueryRepository;

  @Autowired private TestEntityManager entityManager;

  @Nested
  @DisplayName("회원 상세 조회 테스트")
  class DetailQueryTest {

    @Test
    @DisplayName("존재하지 않은 아이디인 경우, 빈 값 반환.")
    void isEmptyWhenMemberIsNotExists() {
      assertThat(memberQueryRepository.findDetailByMemberId(10L)).isEmpty();
    }

    @Test
    @DisplayName("존재하는 아이디인 경우, 계정·프로필·취미 값을 모두 조회한다.")
    void isSuccessWhenMemberIsExists() {
      // Given
      LocalDateTime accessedAt = LocalDateTime.of(2026, 1, 1, 12, 0);
      Member member =
          MemberFixture.builder()
              .yearOfBirth(Calendar.getInstance().get(Calendar.YEAR) - 25)
              .height(20)
              .highestEducation(HighestEducation.ASSOCIATE)
              .nickname("nickname")
              .district(District.DONG_GU_DAEJEON)
              .gender(Gender.MALE)
              .smokingStatus(SmokingStatus.DAILY)
              .mbti(Mbti.ENFJ)
              .drinkingStatus(DrinkingStatus.NONE)
              .religion(Religion.BUDDHIST)
              .job(Job.JOB_SEARCHING)
              .hobbies(Set.of(Hobby.ANIMATION, Hobby.BOARD_GAMES))
              .kakaoId("kakaoId")
              .grade(Grade.DIAMOND)
              .primaryContactType(PrimaryContactType.KAKAO)
              .vip(true)
              .profilePublic(true)
              .datingExamSubmitted(true)
              .lastAccessedAt(accessedAt)
              .build();
      entityManager.persist(member);
      entityManager.persist(MemberFixture.builder().build());
      entityManager.flush();

      // When
      MemberDetail detail = memberQueryRepository.findDetailByMemberId(member.getId()).orElse(null);

      // Then
      assertThat(detail).isNotNull();
      assertThat(detail.memberId()).isEqualTo(member.getId());
      assertThat(detail.activityStatus()).isEqualTo(member.getActivityStatus().name());
      assertThat(detail.grade()).isEqualTo("DIAMOND");
      assertThat(detail.vip()).isTrue();
      assertThat(detail.profilePublic()).isTrue();
      assertThat(detail.datingExamSubmitted()).isTrue();
      assertThat(detail.primaryContactType()).isEqualTo("KAKAO");
      assertThat(detail.phoneNumber()).isEqualTo(member.getPhoneNumber());
      assertThat(detail.kakaoId()).isEqualTo("kakaoId");
      assertThat(detail.nickname()).isEqualTo("nickname");
      assertThat(detail.gender()).isEqualTo("MALE");
      assertThat(detail.yearOfBirth()).isEqualTo(member.getProfile().getYearOfBirth().getValue());
      assertThat(detail.age()).isEqualTo(member.getProfile().getYearOfBirth().getAge());
      assertThat(detail.height()).isEqualTo(20);
      assertThat(detail.job()).isEqualTo("JOB_SEARCHING");
      assertThat(detail.highestEducation()).isEqualTo("ASSOCIATE");
      assertThat(detail.city()).isEqualTo(member.getProfile().getRegion().getCity().name());
      assertThat(detail.district()).isEqualTo("DONG_GU_DAEJEON");
      assertThat(detail.mbti()).isEqualTo("ENFJ");
      assertThat(detail.smokingStatus()).isEqualTo("DAILY");
      assertThat(detail.drinkingStatus()).isEqualTo("NONE");
      assertThat(detail.religion()).isEqualTo("BUDDHIST");
      assertThat(detail.hobbies()).containsExactlyInAnyOrder("ANIMATION", "BOARD_GAMES");
      assertThat(detail.lastAccessedAt()).isEqualTo(accessedAt);
      assertThat(detail.joinedAt()).isCloseTo(member.getCreatedAt(), within(1, ChronoUnit.MICROS));
      assertThat(detail.deletedAt()).isNull();
    }

    @Test
    @DisplayName("접속 기록과 취미가 없는 회원은 lastAccessedAt이 null이고 취미가 비어 있다.")
    void getNullLastAccessedAtAndEmptyHobbies() {
      // Given
      Member member = MemberFixture.builder().hobbies(Set.of()).build();
      entityManager.persist(member);
      entityManager.flush();

      // When
      MemberDetail detail = memberQueryRepository.findDetailByMemberId(member.getId()).orElse(null);

      // Then
      assertThat(detail).isNotNull();
      assertThat(detail.lastAccessedAt()).isNull();
      assertThat(detail.hobbies()).isEmpty();
      assertThat(detail.vip()).isFalse();
      assertThat(detail.profilePublic()).isFalse();
      assertThat(detail.datingExamSubmitted()).isFalse();
    }

    @Test
    @DisplayName("프로필을 입력하지 않은 회원은 프로필 값이 비어 있다.")
    void getDetailWithoutProfile() {
      // Given
      Member member = MemberFixture.builder().noProfile().build();
      entityManager.persist(member);
      entityManager.flush();

      // When
      MemberDetail detail = memberQueryRepository.findDetailByMemberId(member.getId()).orElse(null);

      // Then
      assertThat(detail).isNotNull();
      assertThat(detail.nickname()).isNull();
      assertThat(detail.gender()).isNull();
      assertThat(detail.age()).isNull();
      assertThat(detail.hobbies()).isEmpty();
    }
  }

  @Nested
  @DisplayName("회원 페이지 조회 테스트")
  class PageQueryTest {

    @Test
    @DisplayName("조건이 없으면 모든 회원을 회원 번호가 큰 순서로 조회한다.")
    void findPage() {
      // given
      Member member1 =
          createMember("01011111111", "테스트유저1", ActivityStatus.ACTIVE, Gender.MALE, Grade.SILVER);
      Member member2 =
          createMember(
              "01022222222", "테스트유저2", ActivityStatus.DORMANT, Gender.FEMALE, Grade.DIAMOND);

      // when
      Page<MemberPageItem> result =
          memberQueryRepository.findPage(emptyCondition(), PageRequest.of(0, 10));

      // then
      assertThat(result.getTotalElements()).isEqualTo(2);
      assertThat(result.getContent())
          .extracting(MemberPageItem::memberId)
          .containsExactly(member2.getId(), member1.getId());
      assertPageItem(result.getContent().get(0), member2);
      assertPageItem(result.getContent().get(1), member1);
    }

    @Test
    @DisplayName("offset과 limit만큼만 조회하고 전체 수는 조건에 맞는 회원 수다.")
    void findPageWithOffsetAndLimit() {
      // given
      createMember("01011111111", "테스트유저1", ActivityStatus.ACTIVE, Gender.MALE, Grade.SILVER);
      Member member2 =
          createMember("01022222222", "테스트유저2", ActivityStatus.ACTIVE, Gender.MALE, Grade.SILVER);
      createMember("01033333333", "테스트유저3", ActivityStatus.ACTIVE, Gender.MALE, Grade.SILVER);

      // when
      Page<MemberPageItem> result =
          memberQueryRepository.findPage(emptyCondition(), PageRequest.of(1, 1));

      // then
      assertThat(result.getTotalElements()).isEqualTo(3);
      assertThat(result.getContent())
          .extracting(MemberPageItem::memberId)
          .containsExactly(member2.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"phoneNumber", "nickname", "activityStatus", "gender", "grade"})
    @DisplayName("조건에 맞는 회원만 조회한다.")
    void findPageWithConditions(String conditionType) {
      // given
      Member foundMember =
          createMember("01011111111", "테스트유저1", ActivityStatus.ACTIVE, Gender.MALE, Grade.SILVER);
      createMember("01022222222", "테스트유저2", ActivityStatus.DORMANT, Gender.FEMALE, Grade.DIAMOND);

      MemberPageSearchCondition condition =
          new MemberPageSearchCondition(
              conditionType.equals("activityStatus")
                  ? foundMember.getActivityStatus().name()
                  : null,
              conditionType.equals("gender") ? foundMember.getGender().name() : null,
              conditionType.equals("grade") ? foundMember.getGrade().name() : null,
              conditionType.equals("nickname")
                  ? foundMember.getProfile().getNickname().getValue()
                  : null,
              conditionType.equals("phoneNumber") ? foundMember.getPhoneNumber() : null,
              null,
              null);

      // when
      Page<MemberPageItem> result =
          memberQueryRepository.findPage(condition, PageRequest.of(0, 10));

      // then
      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent()).hasSize(1);
      assertPageItem(result.getContent().get(0), foundMember);
    }

    @Test
    @DisplayName("닉네임은 포함 여부로 찾는다.")
    void findPageWithNicknameKeyword() {
      // given
      Member foundMember =
          createMember("01011111111", "딥플유저", ActivityStatus.ACTIVE, Gender.MALE, Grade.SILVER);
      createMember("01022222222", "다른회원", ActivityStatus.ACTIVE, Gender.MALE, Grade.SILVER);

      // when
      Page<MemberPageItem> result =
          memberQueryRepository.findPage(
              new MemberPageSearchCondition(null, null, null, "플유", null, null, null),
              PageRequest.of(0, 10));

      // then
      assertThat(result.getContent())
          .extracting(MemberPageItem::memberId)
          .containsExactly(foundMember.getId());
    }

    @Test
    @DisplayName("가입일이 시작일과 종료일 사이(양 끝 날짜 포함)인 회원만 조회한다.")
    void findPageWithDateConditions() {
      // given
      Member member =
          createMember("01011111111", "테스트유저1", ActivityStatus.ACTIVE, Gender.MALE, Grade.SILVER);
      LocalDate joinedDate = member.getCreatedAt().toLocalDate();
      Pageable pageable = PageRequest.of(0, 10);

      // when
      Page<MemberPageItem> resultAfterStartDate =
          memberQueryRepository.findPage(dateCondition(joinedDate.plusDays(1), null), pageable);
      Page<MemberPageItem> resultEqStartDate =
          memberQueryRepository.findPage(dateCondition(joinedDate, null), pageable);
      Page<MemberPageItem> resultBeforeEndDate =
          memberQueryRepository.findPage(dateCondition(null, joinedDate.minusDays(1)), pageable);
      Page<MemberPageItem> resultEqEndDate =
          memberQueryRepository.findPage(dateCondition(null, joinedDate), pageable);

      // then
      assertThat(resultAfterStartDate.getTotalElements()).isZero();
      assertThat(resultAfterStartDate.getContent()).isEmpty();

      assertThat(resultEqStartDate.getTotalElements()).isEqualTo(1);
      assertThat(resultEqStartDate.getContent()).hasSize(1);

      assertThat(resultBeforeEndDate.getTotalElements()).isZero();
      assertThat(resultBeforeEndDate.getContent()).isEmpty();

      assertThat(resultEqEndDate.getTotalElements()).isEqualTo(1);
      assertThat(resultEqEndDate.getContent()).hasSize(1);
    }

    private Member createMember(
        String phoneNumber,
        String nickname,
        ActivityStatus activityStatus,
        Gender gender,
        Grade grade) {
      Member member =
          MemberFixture.builder()
              .phoneNumber(phoneNumber)
              .activityStatus(activityStatus)
              .grade(grade)
              .nickname(nickname)
              .gender(gender)
              .build();
      entityManager.persist(member);
      entityManager.flush();
      return member;
    }

    private MemberPageSearchCondition emptyCondition() {
      return new MemberPageSearchCondition(null, null, null, null, null, null, null);
    }

    private MemberPageSearchCondition dateCondition(LocalDate joinedFrom, LocalDate joinedTo) {
      return new MemberPageSearchCondition(null, null, null, null, null, joinedFrom, joinedTo);
    }

    private void assertPageItem(MemberPageItem item, Member member) {
      assertThat(item.memberId()).isEqualTo(member.getId());
      assertThat(item.nickname()).isEqualTo(member.getProfile().getNickname().getValue());
      assertThat(item.activityStatus()).isEqualTo(member.getActivityStatus().name());
      assertThat(item.gender()).isEqualTo(member.getGender().name());
      assertThat(item.joinedAt()).isCloseTo(member.getCreatedAt(), within(1, ChronoUnit.MICROS));
    }
  }

  @Nested
  @DisplayName("프로필 조회 테스트")
  class MyProfileQueryTest {

    @Test
    @DisplayName("존재하지 않은 아이디인 경우, 프로필 빈 값 반환.")
    void isNullWhenMemberIsNotExists() {
      // Given
      Long notExistMemberId = 10L;

      // When & Then
      assertThat(memberQueryRepository.findProfileByMemberId(notExistMemberId)).isEmpty();
    }

    @Test
    @DisplayName("존재하는 경우, 프로필 정보 조회")
    void getProfileWhenMemberIdExists() {
      // Given
      Member member =
          MemberFixture.builder()
              .nickname("nickname")
              .yearOfBirth(1998)
              .gender(Gender.MALE)
              .height(175)
              .job(Job.JOB_SEARCHING)
              .hobbies(Set.of(Hobby.BOARD_GAMES, Hobby.ANIMATION))
              .mbti(Mbti.ISTJ)
              .district(District.ANSAN_SI)
              .smokingStatus(SmokingStatus.DAILY)
              .drinkingStatus(DrinkingStatus.NONE)
              .highestEducation(HighestEducation.ASSOCIATE)
              .religion(Religion.BUDDHIST)
              .build();
      entityManager.persist(member);
      entityManager.flush();
      MemberProfile memberProfile = member.getProfile();

      // When
      MemberProfileView memberProfileView =
          memberQueryRepository.findProfileByMemberId(member.getId()).orElse(null);

      // Then
      assertThat(memberProfileView).isNotNull();
      assertThat(memberProfileView.nickname()).isEqualTo(memberProfile.getNickname().getValue());
      assertThat(memberProfileView.yearOfBirth())
          .isEqualTo(memberProfile.getYearOfBirth().getValue());
      assertThat(memberProfileView.gender()).isEqualTo(memberProfile.getGender().toString());
      assertThat(memberProfileView.height()).isEqualTo(memberProfile.getHeight());
      assertThat(memberProfileView.job()).isEqualTo(memberProfile.getJob().name());
      assertThat(memberProfileView.hobbies())
          .containsAll(memberProfile.getHobbies().stream().map(Hobby::name).toList());
      assertThat(memberProfileView.mbti()).isEqualTo(memberProfile.getMbti().toString());
      assertThat(memberProfileView.city())
          .isEqualTo(memberProfile.getRegion().getCity().toString());
      assertThat(memberProfileView.district())
          .isEqualTo(memberProfile.getRegion().getDistrict().toString());
      assertThat(memberProfileView.smokingStatus())
          .isEqualTo(memberProfile.getSmokingStatus().toString());
      assertThat(memberProfileView.highestEducation())
          .isEqualTo(memberProfile.getHighestEducation().toString());
      assertThat(memberProfileView.religion()).isEqualTo(memberProfile.getReligion().toString());
    }
  }

  @Nested
  @DisplayName("연락처 조회 테스트")
  class ContactQueryTest {

    @Test
    @DisplayName("존재하지 않은 아이디의 경우 연락처 조회 실패.")
    void isFailWhenMemberIsNotExists() {
      // Given
      Long notExistMemberId = 10L;

      // When & Then
      assertThat(memberQueryRepository.findContactsByMemberId(notExistMemberId)).isEmpty();
    }

    @Test
    @DisplayName("아이디가 존재하는 경우 연락처 조회 성공.")
    void isSuccessWhenMemberIsExists() {
      // Given
      Member member =
          MemberFixture.builder()
              .kakaoId("kakaoId")
              .primaryContactType(PrimaryContactType.KAKAO)
              .build();
      entityManager.persist(member);
      entityManager.flush();

      Long existMemberId = member.getId();

      // When
      MemberContactView memberContactView =
          memberQueryRepository.findContactsByMemberId(existMemberId).orElse(null);

      // Then
      assertThat(memberContactView).isNotNull();
      assertThat(memberContactView.phoneNumber()).isEqualTo(member.getPhoneNumber());
      assertThat(memberContactView.kakaoId()).isEqualTo(member.getKakaoId());
      assertThat(memberContactView.primaryContactType())
          .isEqualTo(member.getPrimaryContactType().toString());
    }
  }

  @Nested
  @DisplayName("회원 미리보기 조회 테스트")
  class PreviewQueryTest {

    @Test
    @DisplayName("닉네임·대표 사진·지역·출생연도를 회원 번호별로 조회하고, 대표가 아닌 사진과 다른 회원은 포함하지 않는다.")
    void findPreviewsOfMembers() {
      Member withImage = entityManager.persist(MemberFixture.builder().nickname("사진있음").build());
      Member withoutImage = entityManager.persist(MemberFixture.builder().build());
      Member other = entityManager.persist(MemberFixture.builder().build());
      entityManager.persist(
          ProfileImageFixture.builder()
              .memberId(withImage.getId())
              .imageUrl("https://example.com/primary.jpg")
              .primary(true)
              .build());
      entityManager.persist(
          ProfileImageFixture.builder()
              .memberId(withImage.getId())
              .imageUrl("https://example.com/second.jpg")
              .order(2)
              .primary(false)
              .build());
      entityManager.flush();
      entityManager.clear();

      List<MemberPreview> previews =
          memberQueryRepository.findPreviewsByMemberIds(
              List.of(withImage.getId(), withoutImage.getId()));

      assertThat(previews)
          .extracting(MemberPreview::memberId)
          .containsExactlyInAnyOrder(withImage.getId(), withoutImage.getId())
          .doesNotContain(other.getId());
      MemberPreview preview =
          previews.stream()
              .filter(p -> p.memberId() == withImage.getId())
              .findFirst()
              .orElseThrow();
      assertThat(preview.nickname()).isEqualTo("사진있음");
      assertThat(preview.profileImageUrl()).isEqualTo("https://example.com/primary.jpg");
      assertThat(preview.city()).isEqualTo(withImage.getProfile().getRegion().getCity().name());
      assertThat(preview.yearOfBirth())
          .isEqualTo(withImage.getProfile().getYearOfBirth().getValue());
      assertThat(preview.gender()).isEqualTo(withImage.getProfile().getGender().name());
      assertThat(preview.age())
          .isEqualTo(AgeConverter.toAge(withImage.getProfile().getYearOfBirth().getValue()));
      assertThat(
              previews.stream()
                  .filter(p -> p.memberId() == withoutImage.getId())
                  .findFirst()
                  .orElseThrow()
                  .profileImageUrl())
          .isNull();
    }
  }

  @Nested
  @DisplayName("닉네임·전화번호로 회원 번호 찾기 테스트")
  class FindIdsQueryTest {

    @Test
    @DisplayName("닉네임은 포함 여부로, 전화번호는 일치 여부로 찾고 두 조건을 함께 주면 모두 만족하는 회원만 찾는다.")
    void findIdsByNicknameAndPhoneNumber() {
      Member first =
          entityManager.persist(
              MemberFixture.builder().nickname("검색대상1").phoneNumber("01088880001").build());
      Member second =
          entityManager.persist(
              MemberFixture.builder().nickname("검색대상2").phoneNumber("01088880002").build());
      entityManager.flush();

      assertThat(memberQueryRepository.findIdsByNicknameAndPhoneNumber("검색대상", null))
          .containsExactlyInAnyOrder(first.getId(), second.getId());
      assertThat(memberQueryRepository.findIdsByNicknameAndPhoneNumber(null, "01088880002"))
          .containsExactly(second.getId());
      assertThat(memberQueryRepository.findIdsByNicknameAndPhoneNumber("검색대상1", "01088880002"))
          .isEmpty();
    }
  }

  @Nested
  @DisplayName("관리자 검색 조건으로 회원 번호 찾기 테스트")
  class SearchIdsQueryTest {

    @Test
    @DisplayName("닉네임과 전화번호는 일치 여부로 찾고 두 조건을 함께 주면 모두 만족하는 회원만 찾는다.")
    void searchIdsByNicknameAndPhoneNumber() {
      Member first =
          entityManager.persist(
              MemberFixture.builder().nickname("검색대상1").phoneNumber("01088880001").build());
      Member second =
          entityManager.persist(
              MemberFixture.builder().nickname("검색대상2").phoneNumber("01088880002").build());
      entityManager.flush();

      assertThat(searchIds("검색대상", null, null, null)).isEmpty();
      assertThat(searchIds("검색대상1", null, null, null)).containsExactly(first.getId());
      assertThat(searchIds(null, "01088880002", null, null)).containsExactly(second.getId());
      assertThat(searchIds("검색대상1", "01088880002", null, null)).isEmpty();
      assertThat(searchIds("검색대상2", "01088880002", null, null)).containsExactly(second.getId());
    }

    @Test
    @DisplayName("가입 시각이 시작 시각과 끝 시각 사이(양 끝 포함)인 회원만 찾는다.")
    void searchIdsByJoinedAt() {
      Member joined =
          entityManager.persist(MemberFixture.builder().phoneNumber("01088880003").build());
      entityManager.flush();
      LocalDateTime joinedAt = joined.getCreatedAt();

      assertThat(searchIds(null, "01088880003", joinedAt, joinedAt))
          .containsExactly(joined.getId());
      assertThat(searchIds(null, "01088880003", joinedAt.minusDays(1), null))
          .containsExactly(joined.getId());
      assertThat(searchIds(null, "01088880003", null, joinedAt.plusDays(1)))
          .containsExactly(joined.getId());
      assertThat(searchIds(null, "01088880003", joinedAt.plusSeconds(1), null)).isEmpty();
      assertThat(searchIds(null, "01088880003", null, joinedAt.minusSeconds(1))).isEmpty();
      assertThat(searchIds(null, null, joinedAt.minusDays(1), joinedAt.plusDays(1)))
          .contains(joined.getId());
    }

    private Set<Long> searchIds(
        String nickname, String phoneNumber, LocalDateTime joinedFrom, LocalDateTime joinedTo) {
      return memberQueryRepository.searchIds(
          new MemberIdSearchCondition(nickname, phoneNumber, joinedFrom, joinedTo));
    }
  }

  @Nested
  @DisplayName("심사용 회원 프로필 조회 테스트")
  class FindScreeningProfilesQueryTest {

    @Test
    @DisplayName("요청한 회원의 닉네임·출생 연도·성별·가입 시각을 돌려주고 없는 회원은 빠진다.")
    void findScreeningProfilesByMemberIds() {
      int yearOfBirth = Calendar.getInstance().get(Calendar.YEAR) - 25;
      Member target =
          entityManager.persist(
              MemberFixture.builder()
                  .nickname("심사대상")
                  .yearOfBirth(yearOfBirth)
                  .gender(Gender.FEMALE)
                  .build());
      Member other = entityManager.persist(MemberFixture.builder().build());
      entityManager.flush();
      entityManager.clear();

      List<MemberScreeningProfile> profiles =
          memberQueryRepository.findScreeningProfilesByMemberIds(List.of(target.getId(), -1L));

      assertThat(profiles).hasSize(1);
      MemberScreeningProfile profile = profiles.getFirst();
      assertThat(profile.memberId()).isEqualTo(target.getId());
      assertThat(profile.nickname()).isEqualTo("심사대상");
      assertThat(profile.yearOfBirth()).isEqualTo(yearOfBirth);
      assertThat(profile.gender()).isEqualTo("FEMALE");
      assertThat(profile.joinedAt()).isNotNull();
      assertThat(profiles)
          .extracting(MemberScreeningProfile::memberId)
          .doesNotContain(other.getId());
    }

    @Test
    @DisplayName("프로필을 입력하지 않은 회원은 프로필 값이 비어 있다.")
    void findScreeningProfileWithoutProfile() {
      Member member = entityManager.persist(MemberFixture.builder().noProfile().build());
      entityManager.flush();
      entityManager.clear();

      List<MemberScreeningProfile> profiles =
          memberQueryRepository.findScreeningProfilesByMemberIds(List.of(member.getId()));

      assertThat(profiles).hasSize(1);
      assertThat(profiles.getFirst().nickname()).isNull();
      assertThat(profiles.getFirst().yearOfBirth()).isNull();
      assertThat(profiles.getFirst().gender()).isNull();
    }
  }

  @Nested
  @DisplayName("소개 가능한 회원 번호 조건 검색 테스트")
  class SearchIntroducibleIdsTest {

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
    @DisplayName("조건별로 맞는 회원만 찾고, 비어 있는 조건은 적용하지 않는다.")
    void searchByEachCondition(String fieldName) {
      int currentYear = Calendar.getInstance().get(Calendar.YEAR);
      Member member1 =
          entityManager.persist(
              MemberFixture.builder()
                  .yearOfBirth(currentYear - 19)
                  .hobbies(Set.of(Hobby.ANIMATION, Hobby.BOARD_GAMES))
                  .religion(Religion.BUDDHIST)
                  .district(District.DONG_GU_DAEJEON)
                  .smokingStatus(SmokingStatus.DAILY)
                  .drinkingStatus(DrinkingStatus.SOCIAL)
                  .gender(Gender.MALE)
                  .profilePublic(true)
                  .build());
      Member member2 =
          entityManager.persist(
              MemberFixture.builder()
                  .yearOfBirth(currentYear - 39)
                  .hobbies(Set.of(Hobby.CAMPING, Hobby.BADMINTON_AND_TENNIS))
                  .religion(Religion.NONE)
                  .district(District.GANGNAM_GU)
                  .smokingStatus(SmokingStatus.NONE)
                  .drinkingStatus(DrinkingStatus.NONE)
                  .gender(Gender.FEMALE)
                  .profilePublic(!fieldName.equals("isProfilePublic"))
                  .build());
      entityManager.flush();

      MemberSearchCondition condition =
          new MemberSearchCondition(
              fieldName.equals("excludedIds") ? Set.of(member2.getId()) : Set.of(),
              fieldName.equals("minAge") ? 40 : null,
              fieldName.equals("maxAge") ? 20 : null,
              fieldName.equals("hobbies")
                  ? member1.getProfile().getHobbies().stream()
                      .map(Hobby::name)
                      .collect(Collectors.toSet())
                  : Set.of(),
              fieldName.equals("cities")
                  ? Set.of(member1.getProfile().getRegion().getCity().name())
                  : Set.of(),
              fieldName.equals("religion") ? Religion.BUDDHIST.name() : null,
              fieldName.equals("smokingStatus") ? SmokingStatus.DAILY.name() : null,
              fieldName.equals("drinkingStatus") ? DrinkingStatus.SOCIAL.name() : null,
              fieldName.equals("memberGrade") ? Grade.DIAMOND.name() : null,
              fieldName.equals("gender") ? Gender.MALE.name() : null,
              fieldName.equals("joinedAfter") ? LocalDateTime.now().plusDays(1) : null);

      Set<Long> result = memberQueryRepository.searchIntroducibleIds(condition, 10L);

      switch (fieldName) {
        case "minAge" -> assertThat(result).containsExactly(member2.getId());
        case "memberGrade", "joinedAfter" -> assertThat(result).isEmpty();
        case "null" ->
            assertThat(result).containsExactlyInAnyOrder(member1.getId(), member2.getId());
        default -> assertThat(result).containsExactly(member1.getId());
      }
    }

    @Test
    @DisplayName("조건 집합이 null이어도 적용하지 않고 찾는다.")
    void searchWithNullCollections() {
      Member target = entityManager.persist(MemberFixture.builder().profilePublic(true).build());
      entityManager.flush();

      Set<Long> result =
          memberQueryRepository.searchIntroducibleIds(
              new MemberSearchCondition(
                  null, null, null, null, null, null, null, null, null, null, null),
              10L);

      assertThat(result).containsExactly(target.getId());
    }

    @Test
    @DisplayName("최소·최대 나이를 함께 주면 그 사이 나이의 회원만 찾는다.")
    void searchByAgeRange() {
      int currentYear = Calendar.getInstance().get(Calendar.YEAR);
      Member age24 = persistPublicMember(currentYear - 23);
      Member age25 = persistPublicMember(currentYear - 24);
      Member age30 = persistPublicMember(currentYear - 29);
      Member age31 = persistPublicMember(currentYear - 30);
      entityManager.flush();

      Set<Long> result = memberQueryRepository.searchIntroducibleIds(ageCondition(25, 30), 10L);

      assertThat(result).containsExactlyInAnyOrder(age25.getId(), age30.getId());
      assertThat(result).doesNotContain(age24.getId(), age31.getId());
    }

    @Test
    @DisplayName("활동 중이 아닌 회원은 찾지 않는다.")
    void excludeInactiveMembers() {
      Member active = entityManager.persist(MemberFixture.builder().profilePublic(true).build());
      entityManager.persist(
          MemberFixture.builder()
              .profilePublic(true)
              .activityStatus(ActivityStatus.DORMANT)
              .build());
      entityManager.flush();

      Set<Long> result = memberQueryRepository.searchIntroducibleIds(ageCondition(null, null), 10L);

      assertThat(result).containsExactly(active.getId());
    }

    @Test
    @DisplayName("회원 번호가 큰 순서로 limit만큼만 찾고, 취미가 여러 개 겹쳐도 회원은 한 번만 센다.")
    void limitByIdDescWithDistinctHobbies() {
      Set<Hobby> hobbies = Set.of(Hobby.ANIMATION, Hobby.BOARD_GAMES);
      entityManager.persist(MemberFixture.builder().profilePublic(true).hobbies(hobbies).build());
      Member second =
          entityManager.persist(
              MemberFixture.builder().profilePublic(true).hobbies(hobbies).build());
      Member third =
          entityManager.persist(
              MemberFixture.builder().profilePublic(true).hobbies(hobbies).build());
      entityManager.flush();

      Set<Long> result =
          memberQueryRepository.searchIntroducibleIds(
              new MemberSearchCondition(
                  Set.of(),
                  null,
                  null,
                  Set.of(Hobby.ANIMATION.name(), Hobby.BOARD_GAMES.name()),
                  Set.of(),
                  null,
                  null,
                  null,
                  null,
                  null,
                  null),
              2L);

      assertThat(result).containsExactlyInAnyOrder(second.getId(), third.getId());
    }

    private Member persistPublicMember(int yearOfBirth) {
      return entityManager.persist(
          MemberFixture.builder().profilePublic(true).yearOfBirth(yearOfBirth).build());
    }

    private MemberSearchCondition ageCondition(Integer minAge, Integer maxAge) {
      return new MemberSearchCondition(
          Set.of(), minAge, maxAge, Set.of(), Set.of(), null, null, null, null, null, null);
    }
  }
}
