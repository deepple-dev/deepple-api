package kr.co.deepple.recommendation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.co.deepple.datingexam.application.port.in.SoulmateFinder;
import kr.co.deepple.member.application.port.in.FindMemberProfilesUseCase;
import kr.co.deepple.member.application.port.in.MemberProfileSummary;
import kr.co.deepple.member.application.port.in.MemberSummary;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.member.fixture.MemberSummaryFixture;
import kr.co.deepple.recommendation.adapter.out.persistence.IntroductionQueryRepository;
import kr.co.deepple.recommendation.application.dto.IntroductionSearchCondition;
import kr.co.deepple.recommendation.application.dto.MemberIntroductionProfileQueryResult;
import kr.co.deepple.recommendation.application.dto.MemberIntroductionProfileView;
import kr.co.deepple.recommendation.domain.MemberIdeal;
import kr.co.deepple.recommendation.fixture.MemberIdealFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IntroductionQueryServiceTest {

  @InjectMocks private IntroductionQueryService introductionQueryService;

  @Mock private IntroductionQueryRepository introductionQueryRepository;

  @Mock private IntroductionMemberIdFetcher introductionMemberIdFetcher;

  @Mock private FindMemberProfilesUseCase findMemberProfilesUseCase;

  @Mock private SoulmateFinder soulmateFinder;

  @Captor
  private ArgumentCaptor<IntroductionMemberIdFetcher.IntroductionConditionSupplier<Object>>
      supplierCaptor;

  @Test
  @DisplayName("findDiamondGradeIntroductions 메서드 테스트")
  void findDiamondGradeIntroductionsTest() {
    // given
    long memberId = 1L;
    Set<Long> introductionMemberIds = Set.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);
    when(introductionMemberIdFetcher.fetch(
            eq(memberId),
            eq(IntroductionCacheKeyPrefix.DIAMOND),
            eq("DIAMOND"),
            ArgumentMatchers
                .<IntroductionMemberIdFetcher.IntroductionConditionSupplier<String>>any()))
        .thenReturn(introductionMemberIds);
    List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults = List.of();
    when(introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(
            memberId, introductionMemberIds))
        .thenReturn(memberIntroductionProfileQueryResults);
    List<MemberIntroductionProfileView> expectedResult = List.of();

    // when && then
    try (MockedStatic<MemberIntroductionProfileViewMapper> mockedStatic =
        mockStatic(MemberIntroductionProfileViewMapper.class)) {
      mockedStatic
          .when(() ->
              MemberIntroductionProfileViewMapper.mapWithDefaultTag(
                  memberIntroductionProfileQueryResults))
          .thenReturn(expectedResult);
      List<MemberIntroductionProfileView> result =
          introductionQueryService.findDiamondGradeIntroductions(memberId);
      assertThat(result).isEqualTo(expectedResult);
    }
  }

  @Test
  @DisplayName("findSameHobbyIntroductions 메서드 테스트")
  void findSameHobbyIntroductionsTest() {
    // given
    long memberId = 1L;
    Set<Long> introductionMemberIds = Set.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);
    when(introductionMemberIdFetcher.fetch(
            eq(memberId),
            eq(IntroductionCacheKeyPrefix.SAME_HOBBY),
            isNull(),
            ArgumentMatchers.<IntroductionMemberIdFetcher.IntroductionConditionSupplier<Set>>any()))
        .thenReturn(introductionMemberIds);
    List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults = List.of();
    when(introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(
            memberId, introductionMemberIds))
        .thenReturn(memberIntroductionProfileQueryResults);
    List<MemberIntroductionProfileView> expectedResult = List.of();

    // when && then
    try (MockedStatic<MemberIntroductionProfileViewMapper> mockedStatic =
        mockStatic(MemberIntroductionProfileViewMapper.class)) {
      mockedStatic
          .when(() ->
              MemberIntroductionProfileViewMapper.mapWithSameHobbyTag(
                  memberIntroductionProfileQueryResults))
          .thenReturn(expectedResult);
      List<MemberIntroductionProfileView> result =
          introductionQueryService.findSameHobbyIntroductions(memberId);
      assertThat(result).isEqualTo(expectedResult);
    }
  }

  @Test
  @DisplayName("findSameReligionIntroductions 메서드 테스트")
  void findSameReligionIntroductionsTest() {
    // given
    long memberId = 1L;
    Set<Long> introductionMemberIds = Set.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);
    when(introductionMemberIdFetcher.fetch(
            eq(memberId),
            eq(IntroductionCacheKeyPrefix.SAME_RELIGION),
            isNull(),
            ArgumentMatchers
                .<IntroductionMemberIdFetcher.IntroductionConditionSupplier<String>>any()))
        .thenReturn(introductionMemberIds);
    List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults = List.of();
    when(introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(
            memberId, introductionMemberIds))
        .thenReturn(memberIntroductionProfileQueryResults);
    List<MemberIntroductionProfileView> expectedResult = List.of();

    // when && then
    try (MockedStatic<MemberIntroductionProfileViewMapper> mockedStatic =
        mockStatic(MemberIntroductionProfileViewMapper.class)) {
      mockedStatic
          .when(() ->
              MemberIntroductionProfileViewMapper.mapWithSameReligionTag(
                  memberIntroductionProfileQueryResults))
          .thenReturn(expectedResult);
      List<MemberIntroductionProfileView> result =
          introductionQueryService.findSameReligionIntroductions(memberId);
      assertThat(result).isEqualTo(expectedResult);
    }
  }

  @Test
  @DisplayName("findSameCityIntroductions 메서드 테스트")
  void findSameCityIntroductionsTest() {
    // given
    long memberId = 1L;
    Set<Long> introductionMemberIds = Set.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);
    when(introductionMemberIdFetcher.fetch(
            eq(memberId),
            eq(IntroductionCacheKeyPrefix.SAME_CITY),
            isNull(),
            ArgumentMatchers
                .<IntroductionMemberIdFetcher.IntroductionConditionSupplier<String>>any()))
        .thenReturn(introductionMemberIds);
    List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults = List.of();
    when(introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(
            memberId, introductionMemberIds))
        .thenReturn(memberIntroductionProfileQueryResults);
    List<MemberIntroductionProfileView> expectedResult = List.of();

    // when && then
    try (MockedStatic<MemberIntroductionProfileViewMapper> mockedStatic =
        mockStatic(MemberIntroductionProfileViewMapper.class)) {
      mockedStatic
          .when(() ->
              MemberIntroductionProfileViewMapper.mapWithDefaultTag(
                  memberIntroductionProfileQueryResults))
          .thenReturn(expectedResult);
      List<MemberIntroductionProfileView> result =
          introductionQueryService.findSameCityIntroductions(memberId);
      assertThat(result).isEqualTo(expectedResult);
    }
  }

  @Test
  @DisplayName("findRecentlyJoinedIntroductions 메서드 테스트")
  void findRecentlyJoinedIntroductionsTest() {
    // given
    long memberId = 1L;
    Set<Long> introductionMemberIds = Set.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);
    when(introductionMemberIdFetcher.fetch(
            eq(memberId),
            eq(IntroductionCacheKeyPrefix.RECENTLY_JOINED),
            isNull(),
            ArgumentMatchers
                .<IntroductionMemberIdFetcher.IntroductionConditionSupplier<LocalDateTime>>any()))
        .thenReturn(introductionMemberIds);
    List<MemberIntroductionProfileQueryResult> memberIntroductionProfileQueryResults = List.of();
    when(introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(
            memberId, introductionMemberIds))
        .thenReturn(memberIntroductionProfileQueryResults);
    List<MemberIntroductionProfileView> expectedResult = List.of();

    // when && then
    try (MockedStatic<MemberIntroductionProfileViewMapper> mockedStatic =
        mockStatic(MemberIntroductionProfileViewMapper.class)) {
      mockedStatic
          .when(() ->
              MemberIntroductionProfileViewMapper.mapWithDefaultTag(
                  memberIntroductionProfileQueryResults))
          .thenReturn(expectedResult);
      List<MemberIntroductionProfileView> result =
          introductionQueryService.findRecentlyJoinedIntroductions(memberId);
      assertThat(result).isEqualTo(expectedResult);
    }
  }

  @Test
  @DisplayName("다이아 등급 소개 조건은 회원의 반대 성별과 다이아 등급으로 만든다.")
  void buildDiamondGradeCondition() {
    long memberId = 1L;
    MemberSummary member = memberSummary(memberId);
    MemberIdeal memberIdeal = MemberIdealFixture.builder().memberId(memberId).build();

    introductionQueryService.findDiamondGradeIntroductions(memberId);

    verify(introductionMemberIdFetcher)
        .fetch(
            eq(memberId), eq(IntroductionCacheKeyPrefix.DIAMOND), any(), supplierCaptor.capture());
    IntroductionSearchCondition condition =
        supplierCaptor.getValue().get(Set.of(memberId), memberIdeal, member, "DIAMOND");
    assertThat(condition.getGender()).isEqualTo("FEMALE");
    assertThat(condition.getMemberGrade()).isEqualTo("DIAMOND");
    assertThat(condition.getActivityStatus()).isEqualTo("ACTIVE");
  }

  @Test
  @DisplayName("취미가 같은 이성 소개 조건은 회원 프로필의 취미로 만든다.")
  void buildSameHobbyCondition() {
    long memberId = 1L;
    MemberSummary member = memberSummary(memberId);
    MemberIdeal memberIdeal =
        MemberIdealFixture.builder().memberId(memberId).hobbies(Set.of("WINE")).build();
    when(findMemberProfilesUseCase.findMemberProfiles(List.of(memberId)))
        .thenReturn(Map.of(memberId, profile(memberId, "NONE", Set.of("CAMPING", "DANCE"))));

    introductionQueryService.findSameHobbyIntroductions(memberId);

    verify(introductionMemberIdFetcher)
        .fetch(
            eq(memberId),
            eq(IntroductionCacheKeyPrefix.SAME_HOBBY),
            isNull(),
            supplierCaptor.capture());
    IntroductionSearchCondition condition =
        supplierCaptor.getValue().get(Set.of(memberId), memberIdeal, member, null);
    assertThat(condition.getGender()).isEqualTo("FEMALE");
    assertThat(condition.getHobbies()).containsExactlyInAnyOrder("CAMPING", "DANCE");
  }

  @Test
  @DisplayName("종교가 같은 이성 소개 조건은 회원 프로필의 종교로 만든다.")
  void buildSameReligionCondition() {
    long memberId = 1L;
    MemberSummary member = memberSummary(memberId);
    MemberIdeal memberIdeal =
        MemberIdealFixture.builder().memberId(memberId).religion("BUDDHIST").build();
    when(findMemberProfilesUseCase.findMemberProfiles(List.of(memberId)))
        .thenReturn(Map.of(memberId, profile(memberId, "CATHOLIC", Set.of())));

    introductionQueryService.findSameReligionIntroductions(memberId);

    verify(introductionMemberIdFetcher)
        .fetch(
            eq(memberId),
            eq(IntroductionCacheKeyPrefix.SAME_RELIGION),
            isNull(),
            supplierCaptor.capture());
    IntroductionSearchCondition condition =
        supplierCaptor.getValue().get(Set.of(memberId), memberIdeal, member, null);
    assertThat(condition.getReligion()).isEqualTo("CATHOLIC");
  }

  @Test
  @DisplayName("지역이 같은 이성 소개 조건은 회원의 지역으로 만든다.")
  void buildSameCityCondition() {
    long memberId = 1L;
    MemberSummary member = memberSummary(memberId);
    MemberIdeal memberIdeal =
        MemberIdealFixture.builder().memberId(memberId).cities(Set.of("BUSAN")).build();

    introductionQueryService.findSameCityIntroductions(memberId);

    verify(introductionMemberIdFetcher)
        .fetch(
            eq(memberId),
            eq(IntroductionCacheKeyPrefix.SAME_CITY),
            isNull(),
            supplierCaptor.capture());
    IntroductionSearchCondition condition =
        supplierCaptor.getValue().get(Set.of(memberId), memberIdeal, member, null);
    assertThat(condition.getCities()).containsExactly(member.city());
  }

  @Test
  @DisplayName("소울메이트 소개는 소울메이트 회원의 프로필을 조회한다.")
  void findSoulmateIntroductionsTest() {
    long memberId = 1L;
    Set<Long> soulmateIds = Set.of(2L, 3L);
    when(soulmateFinder.findSoulmateIds(memberId)).thenReturn(soulmateIds);
    when(introductionQueryRepository.findAllMemberIntroductionProfileQueryResultByMemberIds(
            memberId, soulmateIds))
        .thenReturn(List.of());

    List<MemberIntroductionProfileView> result =
        introductionQueryService.findSoulmateIntroductions(memberId);

    assertThat(result).isEmpty();
    verify(introductionQueryRepository)
        .findAllMemberIntroductionProfileQueryResultByMemberIds(memberId, soulmateIds);
  }

  private MemberSummary memberSummary(long memberId) {
    return MemberSummaryFixture.from(
        MemberFixture.builder().id(memberId).gender(Gender.MALE).build());
  }

  private MemberProfileSummary profile(long memberId, String religion, Set<String> hobbies) {
    return new MemberProfileSummary(
        memberId, "닉네임", null, 30, "MALE", "SEOUL", null, null, religion, hobbies);
  }
}
