package kr.co.deepple.profile.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.admin.application.port.in.CountMemberWarningsUseCase;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.interview.application.port.in.FindMemberInterviewAnswersUseCase;
import kr.co.deepple.interview.application.port.in.MemberInterviewAnswerDetail;
import kr.co.deepple.member.application.port.in.FindMemberPageUseCase;
import kr.co.deepple.member.application.port.in.FindMemberScreeningProfilesUseCase;
import kr.co.deepple.member.application.port.in.GetMemberDetailUseCase;
import kr.co.deepple.member.application.port.in.MemberDetail;
import kr.co.deepple.member.application.port.in.MemberPageItem;
import kr.co.deepple.member.application.port.in.MemberPageSearchCondition;
import kr.co.deepple.member.application.port.in.MemberProfileImage;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.notification.application.port.in.FindMemberNotificationSettingUseCase;
import kr.co.deepple.notification.application.port.in.MemberNotificationSetting;
import kr.co.deepple.profile.application.dto.AdminMemberDetailView;
import kr.co.deepple.profile.application.dto.AdminMemberSearchCondition;
import kr.co.deepple.profile.application.dto.AdminMemberSettingInfo;
import kr.co.deepple.profile.application.dto.AdminMemberStatusInfo;
import kr.co.deepple.profile.application.dto.AdminMemberView;
import kr.co.deepple.profile.application.dto.BasicInfo;
import kr.co.deepple.profile.application.dto.HeartBalanceView;
import kr.co.deepple.profile.application.dto.InterviewInfoView;
import kr.co.deepple.profile.application.dto.ProfileInfo;
import kr.co.deepple.profile.fixture.MemberDetailFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AdminMemberQueryServiceTest {

  private static final long MEMBER_ID = 1L;

  @Mock private FindMemberPageUseCase findMemberPageUseCase;
  @Mock private GetMemberDetailUseCase getMemberDetailUseCase;
  @Mock private FindMemberScreeningProfilesUseCase findMemberScreeningProfilesUseCase;
  @Mock private FindMemberInterviewAnswersUseCase findMemberInterviewAnswersUseCase;
  @Mock private CountMemberWarningsUseCase countMemberWarningsUseCase;
  @Mock private FindMemberNotificationSettingUseCase findMemberNotificationSettingUseCase;

  @InjectMocks private AdminMemberQueryService adminMemberQueryService;

  @Nested
  @DisplayName("멤버 페이지네이션 조회 테스트")
  class FindMembersTest {

    @Test
    @DisplayName("회원 페이지의 순서와 전체 수를 유지하고 회원별 경고 수를 붙인다. 경고가 없는 회원은 0이다.")
    void findMembers() {
      // given
      LocalDateTime joinedAt = LocalDateTime.of(2026, 1, 1, 0, 0);
      AdminMemberSearchCondition condition =
          new AdminMemberSearchCondition(
              "ACTIVE",
              "MALE",
              "SILVER",
              "닉네임",
              "01012345678",
              LocalDate.of(2026, 1, 1),
              LocalDate.of(2026, 1, 31));
      Pageable pageable = PageRequest.of(0, 2);
      given(
              findMemberPageUseCase.findMemberPage(
                  new MemberPageSearchCondition(
                      "ACTIVE",
                      "MALE",
                      "SILVER",
                      "닉네임",
                      "01012345678",
                      LocalDate.of(2026, 1, 1),
                      LocalDate.of(2026, 1, 31)),
                  pageable))
          .willReturn(
              new PageImpl<>(
                  List.of(
                      new MemberPageItem(2L, "테스트유저2", "FEMALE", "DORMANT", joinedAt),
                      new MemberPageItem(1L, "테스트유저1", "MALE", "ACTIVE", joinedAt)),
                  pageable,
                  5));
      given(countMemberWarningsUseCase.countMemberWarnings(List.of(2L, 1L)))
          .willReturn(Map.of(1L, 2));

      // when
      Page<AdminMemberView> result = adminMemberQueryService.findMembers(condition, pageable);

      // then
      assertThat(result.getTotalElements()).isEqualTo(5);
      assertThat(result.getPageable()).isEqualTo(pageable);
      assertThat(result.getContent())
          .containsExactly(
              new AdminMemberView(2L, "테스트유저2", "FEMALE", "DORMANT", joinedAt, 0),
              new AdminMemberView(1L, "테스트유저1", "MALE", "ACTIVE", joinedAt, 2));
    }

    @Test
    @DisplayName("조건에 맞는 회원이 없으면 빈 페이지를 돌려준다.")
    void findMembersWithoutResult() {
      // given
      AdminMemberSearchCondition condition =
          new AdminMemberSearchCondition(null, null, null, null, null, null, null);
      Pageable pageable = PageRequest.of(0, 10);
      given(
              findMemberPageUseCase.findMemberPage(
                  new MemberPageSearchCondition(null, null, null, null, null, null, null),
                  pageable))
          .willReturn(new PageImpl<>(List.of(), pageable, 0));

      // when
      Page<AdminMemberView> result = adminMemberQueryService.findMembers(condition, pageable);

      // then
      assertThat(result.getTotalElements()).isZero();
      assertThat(result.getContent()).isEmpty();
    }
  }

  @Nested
  @DisplayName("멤버 단건 조회 테스트")
  class GetMemberDetailTest {

    @Test
    @DisplayName("멤버 단건 조회 성공")
    void getMemberDetail() {
      // given
      LocalDateTime joinedAt = LocalDateTime.of(2026, 1, 1, 0, 0);
      MemberDetail member =
          MemberDetailFixture.builder()
              .memberId(MEMBER_ID)
              .grade("DIAMOND")
              .vip(true)
              .profilePublic(true)
              .datingExamSubmitted(true)
              .primaryContactType("KAKAO")
              .hobbies(Set.of("ANIMATION", "BOARD_GAMES"))
              .joinedAt(joinedAt)
              .build();
      given(getMemberDetailUseCase.getMemberDetail(MEMBER_ID)).willReturn(member);
      given(findMemberInterviewAnswersUseCase.findAllInterviewAnswers(MEMBER_ID))
          .willReturn(
              List.of(
                  new MemberInterviewAnswerDetail(10L, 1L, "질문1", true, "답변1"),
                  new MemberInterviewAnswerDetail(11L, 2L, "비공개 질문", false, "답변2"),
                  new MemberInterviewAnswerDetail(12L, null, null, false, "질문 없는 답변")));
      given(countMemberWarningsUseCase.countMemberWarnings(List.of(MEMBER_ID)))
          .willReturn(Map.of(MEMBER_ID, 2));
      given(findMemberScreeningProfilesUseCase.findProfileImages(MEMBER_ID))
          .willReturn(
              List.of(
                  new MemberProfileImage("https://example.com/image1", 1, true),
                  new MemberProfileImage("https://example.com/image2", 2, false)));
      given(findMemberNotificationSettingUseCase.findMemberNotificationSetting(MEMBER_ID))
          .willReturn(Optional.of(new MemberNotificationSetting(MEMBER_ID, true)));

      // when
      AdminMemberDetailView result = adminMemberQueryService.getMemberDetail(MEMBER_ID);

      // then
      assertThat(result.settingInfo())
          .isEqualTo(new AdminMemberSettingInfo("DIAMOND", true, "ACTIVE", true, true));
      assertThat(result.statusInfo()).isEqualTo(new AdminMemberStatusInfo("KAKAO", true, 2, true));
      assertThat(result.basicInfo())
          .isEqualTo(
              new BasicInfo(
                  member.nickname(),
                  member.gender(),
                  member.kakaoId(),
                  member.age(),
                  member.height(),
                  member.phoneNumber()));
      assertThat(result.heartBalanceInfo()).isEqualTo(new HeartBalanceView(0L, 0L, 0L));
      assertThat(result.profileImageUrls())
          .containsExactly("https://example.com/image1", "https://example.com/image2");
      assertThat(result.profileInfo())
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
      assertThat(result.interviewInfos())
          .containsExactly(
              new InterviewInfoView(1L, "질문1", "답변1"),
              new InterviewInfoView(2L, "비공개 질문", "답변2"),
              new InterviewInfoView(null, null, "질문 없는 답변"));
      assertThat(result.createdAt()).isEqualTo(joinedAt);
      assertThat(result.deletedAt()).isNull();
    }

    @Test
    @DisplayName("인터뷰 답변·경고·사진·알림 설정이 없으면 빈 값과 0, false로 채운다.")
    void getMemberDetailWithoutRelatedData() {
      // given
      LocalDateTime deletedAt = LocalDateTime.of(2026, 2, 1, 0, 0);
      given(getMemberDetailUseCase.getMemberDetail(MEMBER_ID))
          .willReturn(
              MemberDetailFixture.builder().memberId(MEMBER_ID).deletedAt(deletedAt).build());
      given(findMemberNotificationSettingUseCase.findMemberNotificationSetting(MEMBER_ID))
          .willReturn(Optional.empty());

      // when
      AdminMemberDetailView result = adminMemberQueryService.getMemberDetail(MEMBER_ID);

      // then
      assertThat(result.settingInfo().isPushNotificationEnabled()).isFalse();
      assertThat(result.statusInfo().hasInterviewAnswer()).isFalse();
      assertThat(result.statusInfo().warningCount()).isZero();
      assertThat(result.profileImageUrls()).isEmpty();
      assertThat(result.interviewInfos()).isEmpty();
      assertThat(result.deletedAt()).isEqualTo(deletedAt);
    }

    @Test
    @DisplayName("전역 알림을 끈 회원은 푸시 알림 설정이 false다.")
    void getMemberDetailWithNotificationDisabled() {
      // given
      given(getMemberDetailUseCase.getMemberDetail(MEMBER_ID))
          .willReturn(MemberDetailFixture.builder().memberId(MEMBER_ID).build());
      given(findMemberNotificationSettingUseCase.findMemberNotificationSetting(MEMBER_ID))
          .willReturn(Optional.of(new MemberNotificationSetting(MEMBER_ID, false)));

      // when
      AdminMemberDetailView result = adminMemberQueryService.getMemberDetail(MEMBER_ID);

      // then
      assertThat(result.settingInfo().isPushNotificationEnabled()).isFalse();
    }

    @Test
    @DisplayName("멤버 단건 조회 - 멤버가 없는 경우 회원 포트가 던진 오류를 그대로 전한다")
    void getMemberDetailWithoutMember() {
      // given
      DeeppleException memberNotFound =
          new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=1");
      given(getMemberDetailUseCase.getMemberDetail(MEMBER_ID)).willThrow(memberNotFound);

      // when & then
      assertThatThrownBy(() -> adminMemberQueryService.getMemberDetail(MEMBER_ID))
          .isSameAs(memberNotFound);
      verifyNoInteractions(
          findMemberInterviewAnswersUseCase,
          countMemberWarningsUseCase,
          findMemberScreeningProfilesUseCase,
          findMemberNotificationSettingUseCase);
    }
  }
}
