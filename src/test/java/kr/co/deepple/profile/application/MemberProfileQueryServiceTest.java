package kr.co.deepple.profile.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.block.application.port.in.CheckBlockUseCase;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.community.application.port.in.FindProfileExchangeUseCase;
import kr.co.deepple.community.application.port.in.ProfileExchangeSummary;
import kr.co.deepple.interview.application.port.in.FindMemberInterviewAnswersUseCase;
import kr.co.deepple.interview.application.port.in.MemberInterviewAnswer;
import kr.co.deepple.interview.application.port.in.MemberInterviewAnswerDetail;
import kr.co.deepple.like.application.port.in.FindLikeLevelUseCase;
import kr.co.deepple.match.application.port.in.FindMatchBetweenUseCase;
import kr.co.deepple.match.application.port.in.MatchSummary;
import kr.co.deepple.member.application.port.in.FindMemberScreeningProfilesUseCase;
import kr.co.deepple.member.application.port.in.GetMemberDetailUseCase;
import kr.co.deepple.member.application.port.in.MemberDetail;
import kr.co.deepple.member.application.port.in.MemberProfileImage;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.profile.application.dto.BasicInfo;
import kr.co.deepple.profile.application.dto.ContactInfo;
import kr.co.deepple.profile.application.dto.InterviewInfoView;
import kr.co.deepple.profile.application.dto.InterviewResultView;
import kr.co.deepple.profile.application.dto.IntroductionInfo;
import kr.co.deepple.profile.application.dto.MatchInfo;
import kr.co.deepple.profile.application.dto.MemberInfo;
import kr.co.deepple.profile.application.dto.MemberInfoView;
import kr.co.deepple.profile.application.dto.MemberProfileResponse;
import kr.co.deepple.profile.application.dto.ProfileExchangeInfo;
import kr.co.deepple.profile.application.dto.ProfileImageInfo;
import kr.co.deepple.profile.application.dto.ProfileInfo;
import kr.co.deepple.profile.application.dto.StatusInfo;
import kr.co.deepple.profile.domain.ProfileErrorCode;
import kr.co.deepple.profile.fixture.MemberDetailFixture;
import kr.co.deepple.recommendation.application.port.in.FindIntroductionTypeUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberProfileQueryServiceTest {

  private static final long MEMBER_ID = 1L;
  private static final long OTHER_MEMBER_ID = 2L;

  @Mock private GetMemberDetailUseCase getMemberDetailUseCase;
  @Mock private FindMemberScreeningProfilesUseCase findMemberScreeningProfilesUseCase;
  @Mock private FindMemberInterviewAnswersUseCase findMemberInterviewAnswersUseCase;
  @Mock private CheckBlockUseCase checkBlockUseCase;
  @Mock private FindIntroductionTypeUseCase findIntroductionTypeUseCase;
  @Mock private FindMatchBetweenUseCase findMatchBetweenUseCase;
  @Mock private FindLikeLevelUseCase findLikeLevelUseCase;
  @Mock private FindProfileExchangeUseCase findProfileExchangeUseCase;

  @InjectMocks private MemberProfileQueryService memberProfileQueryService;

  @Nested
  @DisplayName("getInfoCache 메서드 테스트.")
  class GetInfoCacheTest {

    @Test
    @DisplayName("회원이 없으면 회원 포트가 던진 오류를 그대로 전하고 인터뷰는 조회하지 않는다.")
    void throwsExceptionWhenMemberNotFound() {
      // Given
      DeeppleException memberNotFound =
          new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=1");
      given(getMemberDetailUseCase.getMemberDetail(MEMBER_ID)).willThrow(memberNotFound);

      // When & Then
      assertThatThrownBy(() -> memberProfileQueryService.getInfoCache(MEMBER_ID))
          .isSameAs(memberNotFound);
      verifyNoInteractions(findMemberInterviewAnswersUseCase);
    }

    @Test
    @DisplayName("회원의 상태·기본·프로필 정보를 응답으로 옮긴다.")
    void returnsMemberInfo() {
      // Given
      MemberDetail member =
          MemberDetailFixture.builder()
              .memberId(MEMBER_ID)
              .vip(true)
              .primaryContactType("KAKAO")
              .datingExamSubmitted(true)
              .hobbies(Set.of("ANIMATION", "BOARD_GAMES"))
              .build();
      given(getMemberDetailUseCase.getMemberDetail(MEMBER_ID)).willReturn(member);

      // When
      MemberInfoView view = memberProfileQueryService.getInfoCache(MEMBER_ID);

      // Then
      assertThat(view.statusInfo())
          .isEqualTo(new StatusInfo(MEMBER_ID, "ACTIVE", true, "KAKAO", true));
      assertThat(view.basicInfo())
          .isEqualTo(
              new BasicInfo(
                  member.nickname(),
                  member.gender(),
                  member.kakaoId(),
                  member.age(),
                  member.height(),
                  member.phoneNumber()));
      assertThat(view.profileInfo())
          .isEqualTo(
              new ProfileInfo(
                  member.job(),
                  member.highestEducation(),
                  member.city(),
                  member.district(),
                  member.mbti(),
                  member.smokingStatus(),
                  member.drinkingStatus(),
                  member.religion(),
                  Set.of("ANIMATION", "BOARD_GAMES")));
    }

    @Test
    @DisplayName("답변마다 인터뷰 정보를 하나씩 담고, 공개 질문의 답변에만 질문 값을 담는다.")
    void returnsInterviewInfoPerAnswer() {
      // Given
      given(getMemberDetailUseCase.getMemberDetail(MEMBER_ID))
          .willReturn(MemberDetailFixture.builder().memberId(MEMBER_ID).build());
      given(findMemberInterviewAnswersUseCase.findAllInterviewAnswers(MEMBER_ID))
          .willReturn(
              List.of(
                  new MemberInterviewAnswerDetail(10L, 1L, "질문1", true, "답변1"),
                  new MemberInterviewAnswerDetail(11L, 2L, "질문2", true, "답변2"),
                  new MemberInterviewAnswerDetail(12L, 2L, "질문2", true, "답변4"),
                  new MemberInterviewAnswerDetail(13L, 3L, "비공개 질문", false, "비공개 답변"),
                  new MemberInterviewAnswerDetail(14L, null, null, false, "질문 없는 답변")));

      // When
      MemberInfoView view = memberProfileQueryService.getInfoCache(MEMBER_ID);

      // Then
      assertThat(view.interviewInfoView())
          .containsExactly(
              new InterviewInfoView(1L, "질문1", "답변1"),
              new InterviewInfoView(2L, "질문2", "답변2"),
              new InterviewInfoView(2L, "질문2", "답변4"),
              new InterviewInfoView(null, null, "비공개 답변"),
              new InterviewInfoView(null, null, "질문 없는 답변"));
    }

    @Test
    @DisplayName("답변이 없으면 값이 모두 빈 인터뷰 정보 하나를 담는다.")
    void returnsSingleEmptyInterviewInfoWhenNoAnswers() {
      // Given
      given(getMemberDetailUseCase.getMemberDetail(MEMBER_ID))
          .willReturn(MemberDetailFixture.builder().memberId(MEMBER_ID).build());
      given(findMemberInterviewAnswersUseCase.findAllInterviewAnswers(MEMBER_ID))
          .willReturn(List.of());

      // When
      MemberInfoView view = memberProfileQueryService.getInfoCache(MEMBER_ID);

      // Then
      assertThat(view.interviewInfoView()).containsExactly(new InterviewInfoView(null, null, null));
    }
  }

  @Nested
  @DisplayName("getMemberProfile 접근 권한 테스트")
  class ProfileAccessTest {

    @Test
    @DisplayName("상대 회원이 없으면 회원 포트가 던진 오류를 그대로 전하고 다른 조회는 하지 않는다.")
    void throwsExceptionWhenOtherMemberNotFound() {
      // Given
      DeeppleException memberNotFound =
          new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=2");
      given(getMemberDetailUseCase.getMemberDetail(OTHER_MEMBER_ID)).willThrow(memberNotFound);

      // When & Then
      assertThatThrownBy(
              () -> memberProfileQueryService.getMemberProfile(MEMBER_ID, OTHER_MEMBER_ID))
          .isSameAs(memberNotFound);
      verifyNoInteractions(
          checkBlockUseCase,
          findIntroductionTypeUseCase,
          findMatchBetweenUseCase,
          findLikeLevelUseCase,
          findProfileExchangeUseCase,
          findMemberInterviewAnswersUseCase,
          findMemberScreeningProfilesUseCase);
    }

    @Test
    @DisplayName("이상형으로 소개받은 경우, 권한 존재.")
    void authorizedWhenIntroduced() {
      givenOtherMember("ACTIVE");
      given(findIntroductionTypeUseCase.findIntroductionType(MEMBER_ID, OTHER_MEMBER_ID))
          .willReturn(Optional.of("DIAMOND_GRADE"));

      assertAuthorized();
    }

    @Test
    @DisplayName("매치 요청을 받은 경우, 권한 존재.")
    void authorizedWhenMatchRequestReceived() {
      givenOtherMember("ACTIVE");
      given(findMatchBetweenUseCase.hasReceivedMatchRequest(MEMBER_ID, OTHER_MEMBER_ID))
          .willReturn(true);

      assertAuthorized();
    }

    @Test
    @DisplayName("좋아요를 받은 경우, 권한 존재.")
    void authorizedWhenLikeReceived() {
      givenOtherMember("ACTIVE");
      given(findLikeLevelUseCase.findLikeLevel(OTHER_MEMBER_ID, MEMBER_ID))
          .willReturn(Optional.of("INTERESTED"));

      assertAuthorized();
    }

    @Test
    @DisplayName("프로필 교환 요청을 받은 경우, 권한 존재.")
    void authorizedWhenProfileExchangeRequestReceived() {
      givenOtherMember("ACTIVE");
      givenProfileExchange(OTHER_MEMBER_ID, MEMBER_ID, "WAITING");

      assertAuthorized();
    }

    @Test
    @DisplayName("프로필 교환 요청을 수락 받은 경우, 권한 존재.")
    void authorizedWhenProfileExchangeApprovedByOther() {
      givenOtherMember("ACTIVE");
      givenProfileExchange(MEMBER_ID, OTHER_MEMBER_ID, "APPROVE");

      assertAuthorized();
    }

    @Test
    @DisplayName("받은 프로필 교환 요청을 수락한 경우, 권한 존재.")
    void authorizedWhenProfileExchangeApprovedByMe() {
      givenOtherMember("ACTIVE");
      givenProfileExchange(OTHER_MEMBER_ID, MEMBER_ID, "APPROVE");

      assertAuthorized();
    }

    @Test
    @DisplayName("받은 프로필 교환 요청을 거절한 경우에도 요청을 받은 회원이므로, 권한 존재.")
    void authorizedWhenProfileExchangeRejectedByMe() {
      givenOtherMember("ACTIVE");
      givenProfileExchange(OTHER_MEMBER_ID, MEMBER_ID, "REJECTED");

      assertAuthorized();
    }

    @Test
    @DisplayName("매치 신청을 한 경우, 권한이 존재하지 않는다.")
    void deniedWhenOnlyMatchRequestSent() {
      givenOtherMember("ACTIVE");
      given(findMatchBetweenUseCase.hasReceivedMatchRequest(MEMBER_ID, OTHER_MEMBER_ID))
          .willReturn(false);

      assertDenied();
      verify(findMatchBetweenUseCase, never()).hasReceivedMatchRequest(OTHER_MEMBER_ID, MEMBER_ID);
    }

    @Test
    @DisplayName("좋아요를 보내기만 한 경우, 권한이 존재하지 않는다.")
    void deniedWhenOnlyLikeSent() {
      givenOtherMember("ACTIVE");
      given(findLikeLevelUseCase.findLikeLevel(OTHER_MEMBER_ID, MEMBER_ID))
          .willReturn(Optional.empty());

      assertDenied();
      verify(findLikeLevelUseCase, never()).findLikeLevel(MEMBER_ID, OTHER_MEMBER_ID);
    }

    @Test
    @DisplayName("프로필 교환 요청이 대기중인 경우, 권한이 존재하지 않는다.")
    void deniedWhenProfileExchangeRequestedByMeIsWaiting() {
      givenOtherMember("ACTIVE");
      givenProfileExchange(MEMBER_ID, OTHER_MEMBER_ID, "WAITING");

      assertDenied();
    }

    @Test
    @DisplayName("프로필 교환 요청이 거절된 경우, 권한이 존재하지 않는다.")
    void deniedWhenProfileExchangeRequestedByMeIsRejected() {
      givenOtherMember("ACTIVE");
      givenProfileExchange(MEMBER_ID, OTHER_MEMBER_ID, "REJECTED");

      assertDenied();
    }

    @Test
    @DisplayName("차단 당한 경우 소개를 받았어도 권한이 존재하지 않는다.")
    void deniedWhenBlocked() {
      givenOtherMember("ACTIVE");
      given(findIntroductionTypeUseCase.findIntroductionType(MEMBER_ID, OTHER_MEMBER_ID))
          .willReturn(Optional.of("DIAMOND_GRADE"));
      given(checkBlockUseCase.isBlocked(OTHER_MEMBER_ID, MEMBER_ID)).willReturn(true);

      assertDenied();
      verify(checkBlockUseCase, never()).isBlocked(MEMBER_ID, OTHER_MEMBER_ID);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(
        strings = {
          "INITIAL",
          "DORMANT",
          "SUSPENDED_TEMPORARILY",
          "SUSPENDED_PERMANENTLY",
          "WAITING_SCREENING",
          "REJECTED_SCREENING"
        })
    @DisplayName("상대가 Active 상태가 아닌 경우 소개를 받았어도 권한이 존재하지 않는다.")
    void deniedWhenOtherMemberIsNotActive(String activityStatus) {
      givenOtherMember(activityStatus);
      given(findIntroductionTypeUseCase.findIntroductionType(MEMBER_ID, OTHER_MEMBER_ID))
          .willReturn(Optional.of("DIAMOND_GRADE"));

      assertDenied();
    }

    @Test
    @DisplayName("어떠한 케이스도 존재하지 않으면, 권한이 존재하지 않는다.")
    void deniedWhenNoRelation() {
      givenOtherMember("ACTIVE");

      assertDenied();
    }

    private void assertAuthorized() {
      assertThatCode(() -> memberProfileQueryService.getMemberProfile(MEMBER_ID, OTHER_MEMBER_ID))
          .doesNotThrowAnyException();
    }

    private void assertDenied() {
      assertThatThrownBy(
              () -> memberProfileQueryService.getMemberProfile(MEMBER_ID, OTHER_MEMBER_ID))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", ProfileErrorCode.PROFILE_ACCESS_DENIED);
      verify(findMatchBetweenUseCase, never()).findActiveMatchBetween(MEMBER_ID, OTHER_MEMBER_ID);
      verifyNoInteractions(findMemberInterviewAnswersUseCase, findMemberScreeningProfilesUseCase);
    }
  }

  @Nested
  @DisplayName("getMemberProfile 응답 조립 테스트")
  class ProfileAssemblyTest {

    @Test
    @DisplayName("상대의 기본 정보와 내가 상대에게 보낸 좋아요 단계, 대표 사진을 담는다.")
    void returnsMemberInfo() {
      // Given
      LocalDateTime accessedAt = LocalDateTime.of(2026, 1, 1, 12, 0);
      MemberDetail otherMember =
          MemberDetailFixture.builder()
              .memberId(OTHER_MEMBER_ID)
              .hobbies(Set.of("ANIMATION", "BOARD_GAMES"))
              .lastAccessedAt(accessedAt)
              .build();
      given(getMemberDetailUseCase.getMemberDetail(OTHER_MEMBER_ID)).willReturn(otherMember);
      givenIntroduced();
      given(findLikeLevelUseCase.findLikeLevel(MEMBER_ID, OTHER_MEMBER_ID))
          .willReturn(Optional.of("HIGHLY_INTERESTED"));
      given(findMemberScreeningProfilesUseCase.findProfileImages(OTHER_MEMBER_ID))
          .willReturn(
              List.of(
                  new MemberProfileImage("https://image/main.jpg", 0, true),
                  new MemberProfileImage("https://image/sub1.jpg", 1, false)));

      // When
      MemberProfileResponse response =
          memberProfileQueryService.getMemberProfile(MEMBER_ID, OTHER_MEMBER_ID);

      // Then
      assertThat(response.memberInfo())
          .isEqualTo(
              new MemberInfo(
                  OTHER_MEMBER_ID,
                  otherMember.nickname(),
                  "https://image/main.jpg",
                  otherMember.age(),
                  otherMember.gender(),
                  otherMember.height(),
                  otherMember.job(),
                  Set.of("ANIMATION", "BOARD_GAMES"),
                  otherMember.mbti(),
                  otherMember.city(),
                  otherMember.district(),
                  otherMember.smokingStatus(),
                  otherMember.drinkingStatus(),
                  otherMember.highestEducation(),
                  otherMember.religion(),
                  "HIGHLY_INTERESTED",
                  accessedAt,
                  List.of(new ProfileImageInfo("https://image/sub1.jpg", 1))));
      verify(findLikeLevelUseCase, never()).findLikeLevel(OTHER_MEMBER_ID, MEMBER_ID);
    }

    @Test
    @DisplayName("좋아요·접속 기록·대표 사진이 없으면 해당 값은 비어 있다.")
    void returnsNullsWhenNoLikeAndNoAccessAndNoPrimaryImage() {
      // Given
      givenOtherMember("ACTIVE");
      givenIntroduced();

      // When
      MemberProfileResponse response =
          memberProfileQueryService.getMemberProfile(MEMBER_ID, OTHER_MEMBER_ID);

      // Then
      assertThat(response.memberInfo().likeLevel()).isNull();
      assertThat(response.memberInfo().lastAccessedAt()).isNull();
      assertThat(response.memberInfo().profileImageUrl()).isNull();
      assertThat(response.memberInfo().additionalProfileImages()).isEmpty();
    }

    @Test
    @DisplayName("상대방 프로필 조회 시 메인 이미지를 제외한 추가 이미지를 순서대로 반환한다.")
    void returnsAdditionalProfileImagesExcludingPrimary() {
      // Given
      givenOtherMember("ACTIVE");
      givenIntroduced();
      given(findMemberScreeningProfilesUseCase.findProfileImages(OTHER_MEMBER_ID))
          .willReturn(
              List.of(
                  new MemberProfileImage("https://image/main.jpg", 0, true),
                  new MemberProfileImage("https://image/sub1.jpg", 1, false),
                  new MemberProfileImage("https://image/sub2.jpg", 2, false)));

      // When
      MemberProfileResponse response =
          memberProfileQueryService.getMemberProfile(MEMBER_ID, OTHER_MEMBER_ID);

      // Then
      assertThat(response.memberInfo().additionalProfileImages())
          .extracting(ProfileImageInfo::url, ProfileImageInfo::order)
          .containsExactly(tuple("https://image/sub1.jpg", 1), tuple("https://image/sub2.jpg", 2));
    }

    @Test
    @DisplayName("메인 이미지만 존재하는 경우, 추가 이미지 목록은 비어 있다.")
    void returnsEmptyAdditionalProfileImagesWhenOnlyPrimaryExists() {
      // Given
      givenOtherMember("ACTIVE");
      givenIntroduced();
      given(findMemberScreeningProfilesUseCase.findProfileImages(OTHER_MEMBER_ID))
          .willReturn(List.of(new MemberProfileImage("https://image/main.jpg", 0, true)));

      // When
      MemberProfileResponse response =
          memberProfileQueryService.getMemberProfile(MEMBER_ID, OTHER_MEMBER_ID);

      // Then
      assertThat(response.memberInfo().profileImageUrl()).isEqualTo("https://image/main.jpg");
      assertThat(response.memberInfo().additionalProfileImages()).isEmpty();
    }

    @Test
    @DisplayName("상대방과의 매치가 존재하지 않은 경우, 매치 정보와 연락처는 비어 있다.")
    void returnsNoMatchInfoWhenMatchIsNotExists() {
      // Given
      givenOtherMember("ACTIVE");
      givenIntroduced();

      // When
      MemberProfileResponse response =
          memberProfileQueryService.getMemberProfile(MEMBER_ID, OTHER_MEMBER_ID);

      // Then
      assertThat(response.matchInfo()).isNull();
      assertThat(response.contactInfo()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"WAITING", "REJECTED"})
    @DisplayName("매치가 수락되지 않은 경우, 연락처를 제외한 매치 정보를 함께 조회.")
    void returnsMatchInfoWithoutContactWhenNotMatched(String status) {
      // Given
      givenOtherMember("ACTIVE");
      givenIntroduced();
      given(findMatchBetweenUseCase.findActiveMatchBetween(MEMBER_ID, OTHER_MEMBER_ID))
          .willReturn(
              Optional.of(
                  new MatchSummary(
                      10L,
                      MEMBER_ID,
                      OTHER_MEMBER_ID,
                      "매치 신청합니다.",
                      null,
                      status,
                      "PHONE_NUMBER",
                      null)));

      // When
      MemberProfileResponse response =
          memberProfileQueryService.getMemberProfile(MEMBER_ID, OTHER_MEMBER_ID);

      // Then
      assertThat(response.matchInfo())
          .isEqualTo(
              new MatchInfo(
                  10L,
                  MEMBER_ID,
                  OTHER_MEMBER_ID,
                  "매치 신청합니다.",
                  null,
                  status,
                  "PHONE_NUMBER",
                  null));
      assertThat(response.contactInfo()).isNull();
    }

    @Test
    @DisplayName("상대방이 매치를 수락한 경우, 응답자인 상대가 고른 연락 수단의 연락처를 포함한다.")
    void returnsResponderContactWhenOtherMemberIsResponder() {
      // Given
      given(getMemberDetailUseCase.getMemberDetail(OTHER_MEMBER_ID))
          .willReturn(
              MemberDetailFixture.builder()
                  .memberId(OTHER_MEMBER_ID)
                  .phoneNumber("01099998888")
                  .kakaoId("otherKakao")
                  .build());
      givenIntroduced();
      given(findMatchBetweenUseCase.findActiveMatchBetween(MEMBER_ID, OTHER_MEMBER_ID))
          .willReturn(
              Optional.of(
                  new MatchSummary(
                      10L,
                      MEMBER_ID,
                      OTHER_MEMBER_ID,
                      "매치 신청합니다.",
                      "매치 수락합니다!",
                      "MATCHED",
                      "PHONE_NUMBER",
                      "KAKAO")));

      // When
      MemberProfileResponse response =
          memberProfileQueryService.getMemberProfile(MEMBER_ID, OTHER_MEMBER_ID);

      // Then
      assertThat(response.matchInfo())
          .isEqualTo(
              new MatchInfo(
                  10L,
                  MEMBER_ID,
                  OTHER_MEMBER_ID,
                  "매치 신청합니다.",
                  "매치 수락합니다!",
                  "MATCHED",
                  "PHONE_NUMBER",
                  "KAKAO"));
      assertThat(response.contactInfo()).isEqualTo(new ContactInfo("KAKAO", "otherKakao"));
    }

    @Test
    @DisplayName("상대방의 매치 요청을 수락한 경우, 요청자인 상대가 고른 연락 수단의 연락처를 포함한다.")
    void returnsRequesterContactWhenOtherMemberIsRequester() {
      // Given
      given(getMemberDetailUseCase.getMemberDetail(OTHER_MEMBER_ID))
          .willReturn(
              MemberDetailFixture.builder()
                  .memberId(OTHER_MEMBER_ID)
                  .phoneNumber("01099998888")
                  .kakaoId("otherKakao")
                  .build());
      given(findMatchBetweenUseCase.hasReceivedMatchRequest(MEMBER_ID, OTHER_MEMBER_ID))
          .willReturn(true);
      given(findMatchBetweenUseCase.findActiveMatchBetween(MEMBER_ID, OTHER_MEMBER_ID))
          .willReturn(
              Optional.of(
                  new MatchSummary(
                      10L,
                      OTHER_MEMBER_ID,
                      MEMBER_ID,
                      "매치 신청합니다.",
                      "매치 수락합니다!",
                      "MATCHED",
                      "PHONE_NUMBER",
                      "KAKAO")));

      // When
      MemberProfileResponse response =
          memberProfileQueryService.getMemberProfile(MEMBER_ID, OTHER_MEMBER_ID);

      // Then
      assertThat(response.contactInfo()).isEqualTo(new ContactInfo("PHONE_NUMBER", "01099998888"));
    }

    @Test
    @DisplayName("상대방과의 프로필 요청이 존재하는 경우, 프로필 교환 요청 정보를 포함.")
    void returnsProfileExchangeInfoWhenProfileExchangeExists() {
      // Given
      givenOtherMember("ACTIVE");
      givenIntroduced();
      given(findProfileExchangeUseCase.findProfileExchangeBetween(MEMBER_ID, OTHER_MEMBER_ID))
          .willReturn(
              Optional.of(new ProfileExchangeSummary(20L, MEMBER_ID, OTHER_MEMBER_ID, "WAITING")));

      // When
      MemberProfileResponse response =
          memberProfileQueryService.getMemberProfile(MEMBER_ID, OTHER_MEMBER_ID);

      // Then
      assertThat(response.profileExchangeInfo())
          .isEqualTo(new ProfileExchangeInfo(20L, MEMBER_ID, OTHER_MEMBER_ID, "WAITING"));
    }

    @Test
    @DisplayName("소개받은 경우 소개 종류를 포함하고, 프로필 교환이 없으면 교환 정보는 비어 있다.")
    void returnsIntroductionInfoWhenIntroduced() {
      // Given
      givenOtherMember("ACTIVE");
      givenIntroduced();

      // When
      MemberProfileResponse response =
          memberProfileQueryService.getMemberProfile(MEMBER_ID, OTHER_MEMBER_ID);

      // Then
      assertThat(response.introductionInfo()).isEqualTo(new IntroductionInfo("DIAMOND_GRADE"));
      assertThat(response.profileExchangeInfo()).isNull();
    }

    @Test
    @DisplayName("소개받지 않은 경우, 소개 정보는 비어 있다.")
    void returnsNoIntroductionInfoWhenNotIntroduced() {
      // Given
      givenOtherMember("ACTIVE");
      given(findLikeLevelUseCase.findLikeLevel(OTHER_MEMBER_ID, MEMBER_ID))
          .willReturn(Optional.of("INTERESTED"));

      // When
      MemberProfileResponse response =
          memberProfileQueryService.getMemberProfile(MEMBER_ID, OTHER_MEMBER_ID);

      // Then
      assertThat(response.introductionInfo()).isNull();
    }

    @Test
    @DisplayName("상대가 공개 질문에 남긴 답변을 질문·카테고리와 함께 순서대로 담는다.")
    void returnsInterviews() {
      // Given
      givenOtherMember("ACTIVE");
      givenIntroduced();
      given(findMemberInterviewAnswersUseCase.findPublicInterviewAnswers(OTHER_MEMBER_ID))
          .willReturn(
              List.of(
                  new MemberInterviewAnswer(1L, "인터뷰 질문 내용1", "PERSONAL", 10L, "인터뷰 질문 답변1"),
                  new MemberInterviewAnswer(2L, "인터뷰 질문 내용2", "ROMANTIC", 11L, "인터뷰 질문 답변2")));

      // When
      MemberProfileResponse response =
          memberProfileQueryService.getMemberProfile(MEMBER_ID, OTHER_MEMBER_ID);

      // Then
      assertThat(response.interviews())
          .containsExactly(
              new InterviewResultView("인터뷰 질문 내용1", "PERSONAL", "인터뷰 질문 답변1"),
              new InterviewResultView("인터뷰 질문 내용2", "ROMANTIC", "인터뷰 질문 답변2"));
    }

    @Test
    @DisplayName("상대의 인터뷰 답변이 없으면 인터뷰 목록은 비어 있다.")
    void returnsEmptyInterviewsWhenNoAnswers() {
      // Given
      givenOtherMember("ACTIVE");
      givenIntroduced();

      // When
      MemberProfileResponse response =
          memberProfileQueryService.getMemberProfile(MEMBER_ID, OTHER_MEMBER_ID);

      // Then
      assertThat(response.interviews()).isEmpty();
    }

    private void givenIntroduced() {
      given(findIntroductionTypeUseCase.findIntroductionType(MEMBER_ID, OTHER_MEMBER_ID))
          .willReturn(Optional.of("DIAMOND_GRADE"));
    }
  }

  private void givenOtherMember(String activityStatus) {
    given(getMemberDetailUseCase.getMemberDetail(OTHER_MEMBER_ID))
        .willReturn(
            MemberDetailFixture.builder()
                .memberId(OTHER_MEMBER_ID)
                .activityStatus(activityStatus)
                .build());
  }

  private void givenProfileExchange(long requesterId, long responderId, String status) {
    given(findProfileExchangeUseCase.findProfileExchangeBetween(MEMBER_ID, OTHER_MEMBER_ID))
        .willReturn(Optional.of(new ProfileExchangeSummary(20L, requesterId, responderId, status)));
  }
}
