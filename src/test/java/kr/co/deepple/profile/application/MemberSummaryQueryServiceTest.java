package kr.co.deepple.profile.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.block.application.port.in.CheckBlockUseCase;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.community.application.port.in.FindProfileExchangeUseCase;
import kr.co.deepple.community.application.port.in.ProfileExchangeSummary;
import kr.co.deepple.datingexam.application.port.in.DominantPersonalityType;
import kr.co.deepple.datingexam.application.port.in.FindDominantPersonalityTypesUseCase;
import kr.co.deepple.like.application.port.in.FindLikeLevelUseCase;
import kr.co.deepple.member.application.port.in.FindMemberScreeningProfilesUseCase;
import kr.co.deepple.member.application.port.in.GetMemberDetailUseCase;
import kr.co.deepple.member.application.port.in.MemberDetail;
import kr.co.deepple.member.application.port.in.MemberProfileImage;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.profile.application.dto.MemberSummaryView;
import kr.co.deepple.profile.domain.ProfileErrorCode;
import kr.co.deepple.profile.fixture.MemberDetailFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberSummaryQueryServiceTest {

  private static final long MEMBER_ID = 1L;
  private static final long OTHER_MEMBER_ID = 2L;

  @Mock private GetMemberDetailUseCase getMemberDetailUseCase;
  @Mock private FindMemberScreeningProfilesUseCase findMemberScreeningProfilesUseCase;
  @Mock private FindDominantPersonalityTypesUseCase findDominantPersonalityTypesUseCase;
  @Mock private FindLikeLevelUseCase findLikeLevelUseCase;
  @Mock private FindProfileExchangeUseCase findProfileExchangeUseCase;
  @Mock private CheckBlockUseCase checkBlockUseCase;

  @InjectMocks private MemberSummaryQueryService memberSummaryQueryService;

  @Test
  @DisplayName("상대의 프로필, 대표 유형, 대표 사진, 내가 보낸 좋아요, 프로필 교환 상태를 모아 응답한다.")
  void returnsMemberSummary() {
    // Given
    MemberDetail other =
        MemberDetailFixture.builder()
            .memberId(OTHER_MEMBER_ID)
            .nickname("한강러너")
            .gender("FEMALE")
            .yearOfBirth(1995)
            .city("SEOUL")
            .district("DONGJAK_GU")
            .mbti("ISTP")
            .hobbies(Set.of("HIKING_AND_CLIMBING"))
            .build();
    given(getMemberDetailUseCase.getMemberDetail(OTHER_MEMBER_ID)).willReturn(other);
    given(findDominantPersonalityTypesUseCase.findDominantPersonalityType(OTHER_MEMBER_ID))
        .willReturn(Optional.of(new DominantPersonalityType("DECISIVE_INDEPENDENT", "자기주도형")));
    given(findMemberScreeningProfilesUseCase.findProfileImages(OTHER_MEMBER_ID))
        .willReturn(
            List.of(
                new MemberProfileImage("https://cdn/second.webp", 2, false),
                new MemberProfileImage("https://cdn/primary.webp", 1, true)));
    given(findLikeLevelUseCase.findLikeLevel(MEMBER_ID, OTHER_MEMBER_ID))
        .willReturn(Optional.of("INTERESTED"));
    given(findProfileExchangeUseCase.findProfileExchangeBetween(MEMBER_ID, OTHER_MEMBER_ID))
        .willReturn(
            Optional.of(new ProfileExchangeSummary(10L, MEMBER_ID, OTHER_MEMBER_ID, "WAITING")));

    // When
    MemberSummaryView view = memberSummaryQueryService.getMemberSummary(MEMBER_ID, OTHER_MEMBER_ID);

    // Then
    assertThat(view)
        .isEqualTo(
            new MemberSummaryView(
                OTHER_MEMBER_ID,
                "한강러너",
                "DECISIVE_INDEPENDENT",
                "자기주도형",
                "https://cdn/primary.webp",
                other.age(),
                "FEMALE",
                "SEOUL",
                "DONGJAK_GU",
                "ISTP",
                Set.of("HIKING_AND_CLIMBING"),
                "INTERESTED",
                "WAITING",
                "SENT"));
  }

  @Test
  @DisplayName("대표 유형·대표 사진·좋아요·프로필 교환이 없으면 null로 응답한다.")
  void returnsNullWhenOptionalValuesMissing() {
    // Given
    given(getMemberDetailUseCase.getMemberDetail(OTHER_MEMBER_ID))
        .willReturn(MemberDetailFixture.builder().memberId(OTHER_MEMBER_ID).build());
    given(findDominantPersonalityTypesUseCase.findDominantPersonalityType(OTHER_MEMBER_ID))
        .willReturn(Optional.empty());
    given(findMemberScreeningProfilesUseCase.findProfileImages(OTHER_MEMBER_ID))
        .willReturn(List.of(new MemberProfileImage("https://cdn/other.webp", 1, false)));
    given(findLikeLevelUseCase.findLikeLevel(MEMBER_ID, OTHER_MEMBER_ID))
        .willReturn(Optional.empty());
    given(findProfileExchangeUseCase.findProfileExchangeBetween(MEMBER_ID, OTHER_MEMBER_ID))
        .willReturn(Optional.empty());

    // When
    MemberSummaryView view = memberSummaryQueryService.getMemberSummary(MEMBER_ID, OTHER_MEMBER_ID);

    // Then
    assertThat(view.personalityType()).isNull();
    assertThat(view.personalityTypeName()).isNull();
    assertThat(view.profileImageUrl()).isNull();
    assertThat(view.likeLevel()).isNull();
    assertThat(view.profileExchangeStatus()).isNull();
    assertThat(view.profileExchangeDirection()).isNull();
  }

  @Test
  @DisplayName("상대가 프로필 교환을 신청했으면 신청 방향을 RECEIVED로 응답한다.")
  void returnsReceivedWhenOtherRequestedProfileExchange() {
    // Given
    given(getMemberDetailUseCase.getMemberDetail(OTHER_MEMBER_ID))
        .willReturn(MemberDetailFixture.builder().memberId(OTHER_MEMBER_ID).build());
    given(findProfileExchangeUseCase.findProfileExchangeBetween(MEMBER_ID, OTHER_MEMBER_ID))
        .willReturn(
            Optional.of(new ProfileExchangeSummary(10L, OTHER_MEMBER_ID, MEMBER_ID, "WAITING")));

    // When
    MemberSummaryView view = memberSummaryQueryService.getMemberSummary(MEMBER_ID, OTHER_MEMBER_ID);

    // Then
    assertThat(view.profileExchangeStatus()).isEqualTo("WAITING");
    assertThat(view.profileExchangeDirection()).isEqualTo("RECEIVED");
  }

  @Test
  @DisplayName("내가 상대를 차단했으면 거절한다.")
  void deniesWhenIBlockedOther() {
    // Given
    given(getMemberDetailUseCase.getMemberDetail(OTHER_MEMBER_ID))
        .willReturn(MemberDetailFixture.builder().memberId(OTHER_MEMBER_ID).build());
    given(checkBlockUseCase.isBlocked(MEMBER_ID, OTHER_MEMBER_ID)).willReturn(true);

    // When & Then
    assertAccessDenied();
  }

  @Test
  @DisplayName("상대가 나를 차단했으면 거절한다.")
  void deniesWhenOtherBlockedMe() {
    // Given
    given(getMemberDetailUseCase.getMemberDetail(OTHER_MEMBER_ID))
        .willReturn(MemberDetailFixture.builder().memberId(OTHER_MEMBER_ID).build());
    given(checkBlockUseCase.isBlocked(MEMBER_ID, OTHER_MEMBER_ID)).willReturn(false);
    given(checkBlockUseCase.isBlocked(OTHER_MEMBER_ID, MEMBER_ID)).willReturn(true);

    // When & Then
    assertAccessDenied();
  }

  @Test
  @DisplayName("상대가 활동 중이 아니면 거절한다.")
  void deniesWhenOtherNotActive() {
    // Given
    given(getMemberDetailUseCase.getMemberDetail(OTHER_MEMBER_ID))
        .willReturn(
            MemberDetailFixture.builder()
                .memberId(OTHER_MEMBER_ID)
                .activityStatus("SUSPENDED_TEMPORARILY")
                .build());

    // When & Then
    assertAccessDenied();
  }

  @Test
  @DisplayName("상대가 없으면 회원 포트가 던진 오류를 그대로 전한다.")
  void throwsWhenOtherNotFound() {
    // Given
    DeeppleException memberNotFound =
        new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + OTHER_MEMBER_ID);
    given(getMemberDetailUseCase.getMemberDetail(OTHER_MEMBER_ID)).willThrow(memberNotFound);

    // When & Then
    assertThatThrownBy(() -> memberSummaryQueryService.getMemberSummary(MEMBER_ID, OTHER_MEMBER_ID))
        .isSameAs(memberNotFound);
    verifyNoInteractions(checkBlockUseCase, findLikeLevelUseCase, findProfileExchangeUseCase);
  }

  private void assertAccessDenied() {
    assertThatThrownBy(() -> memberSummaryQueryService.getMemberSummary(MEMBER_ID, OTHER_MEMBER_ID))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", ProfileErrorCode.PROFILE_ACCESS_DENIED);
    verifyNoInteractions(
        findDominantPersonalityTypesUseCase,
        findMemberScreeningProfilesUseCase,
        findLikeLevelUseCase,
        findProfileExchangeUseCase);
  }
}
