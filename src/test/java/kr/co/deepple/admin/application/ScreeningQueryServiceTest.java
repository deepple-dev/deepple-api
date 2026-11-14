package kr.co.deepple.admin.application;

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
import kr.co.deepple.admin.adapter.out.persistence.ScreeningDetailQueryRepository;
import kr.co.deepple.admin.adapter.out.persistence.ScreeningQueryRepository;
import kr.co.deepple.admin.application.dto.RawScreeningDetailView;
import kr.co.deepple.admin.application.dto.RawScreeningView;
import kr.co.deepple.admin.application.dto.ScreeningDetailInterviewView;
import kr.co.deepple.admin.application.dto.ScreeningDetailProfileImageView;
import kr.co.deepple.admin.application.dto.ScreeningDetailProfileView;
import kr.co.deepple.admin.application.dto.ScreeningDetailView;
import kr.co.deepple.admin.application.dto.ScreeningSearchCondition;
import kr.co.deepple.admin.application.dto.ScreeningView;
import kr.co.deepple.admin.domain.AdminErrorCode;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.interview.application.port.in.FindMemberInterviewAnswersUseCase;
import kr.co.deepple.interview.application.port.in.MemberInterviewAnswer;
import kr.co.deepple.member.application.port.in.FindMemberScreeningProfilesUseCase;
import kr.co.deepple.member.application.port.in.MemberIdSearchCondition;
import kr.co.deepple.member.application.port.in.MemberProfileImage;
import kr.co.deepple.member.application.port.in.MemberScreeningProfile;
import kr.co.deepple.member.application.port.in.SearchMemberIdsUseCase;
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
class ScreeningQueryServiceTest {

  private static final Pageable PAGEABLE = PageRequest.of(0, 10);
  private static final LocalDateTime JOINED_AT = LocalDateTime.of(2026, 1, 2, 3, 4, 5, 123_456_000);
  private static final String JOINED_DATE = "2026-01-02 03:04:05.123456";

  @Mock private ScreeningQueryRepository screeningQueryRepository;
  @Mock private ScreeningDetailQueryRepository screeningDetailQueryRepository;
  @Mock private SearchMemberIdsUseCase searchMemberIdsUseCase;
  @Mock private FindMemberScreeningProfilesUseCase findMemberScreeningProfilesUseCase;
  @Mock private FindMemberInterviewAnswersUseCase findMemberInterviewAnswersUseCase;

  @InjectMocks private ScreeningQueryService screeningQueryService;

  @Nested
  @DisplayName("심사 목록 조회")
  class FindScreenings {

    @Test
    @DisplayName("조건 없이 조회하면 회원을 검색하지 않고 닉네임·성별·가입일을 붙여 돌려준다.")
    void findAllScreenings() {
      var condition = new ScreeningSearchCondition(null, null, null, null, null);
      given(screeningQueryRepository.findScreenings(null, null, PAGEABLE))
          .willReturn(
              new PageImpl<>(
                  List.of(
                      new RawScreeningView(2L, 102L, "REJECTED", "STOLEN_IMAGE"),
                      new RawScreeningView(1L, 101L, "PENDING", null)),
                  PAGEABLE,
                  12));
      given(findMemberScreeningProfilesUseCase.findMemberScreeningProfiles(Set.of(101L, 102L)))
          .willReturn(
              Map.of(
                  101L, member(101L, "member1", "MALE"), 102L, member(102L, "member2", "FEMALE")));

      Page<ScreeningView> screenings = screeningQueryService.findScreenings(condition, PAGEABLE);

      assertThat(screenings.getTotalElements()).isEqualTo(12);
      assertThat(screenings.getPageable()).isEqualTo(PAGEABLE);
      assertThat(screenings.getContent())
          .containsExactly(
              new ScreeningView(2L, "member2", "FEMALE", JOINED_DATE, "REJECTED", "STOLEN_IMAGE"),
              new ScreeningView(1L, "member1", "MALE", JOINED_DATE, "PENDING", null));
      verifyNoInteractions(searchMemberIdsUseCase);
    }

    @Test
    @DisplayName("심사 상태 조건은 그대로 넘겨 조회한다.")
    void findScreeningsByStatus() {
      var condition = new ScreeningSearchCondition("APPROVED", null, null, null, null);
      given(screeningQueryRepository.findScreenings("APPROVED", null, PAGEABLE))
          .willReturn(new PageImpl<>(List.of(), PAGEABLE, 0));
      given(findMemberScreeningProfilesUseCase.findMemberScreeningProfiles(Set.of()))
          .willReturn(Map.of());

      Page<ScreeningView> screenings = screeningQueryService.findScreenings(condition, PAGEABLE);

      assertThat(screenings.getTotalElements()).isZero();
      verifyNoInteractions(searchMemberIdsUseCase);
    }

    @Test
    @DisplayName("닉네임 조건은 닉네임이 일치하는 회원 번호로 바꿔 조회한다.")
    void findScreeningsByNickname() {
      var condition = new ScreeningSearchCondition(null, "member1", null, null, null);
      given(
              searchMemberIdsUseCase.searchMemberIds(
                  new MemberIdSearchCondition("member1", null, null, null)))
          .willReturn(Set.of(101L));
      givenScreeningOfMember1(null, Set.of(101L));

      Page<ScreeningView> screenings = screeningQueryService.findScreenings(condition, PAGEABLE);

      assertThat(screenings.getTotalElements()).isEqualTo(1);
      assertThat(screenings.getContent().getFirst().nickname()).isEqualTo("member1");
    }

    @Test
    @DisplayName("전화번호 조건은 전화번호가 일치하는 회원 번호로 바꿔 조회한다.")
    void findScreeningsByPhoneNumber() {
      var condition = new ScreeningSearchCondition(null, null, "01011111111", null, null);
      given(
              searchMemberIdsUseCase.searchMemberIds(
                  new MemberIdSearchCondition(null, "01011111111", null, null)))
          .willReturn(Set.of(101L));
      givenScreeningOfMember1(null, Set.of(101L));

      Page<ScreeningView> screenings = screeningQueryService.findScreenings(condition, PAGEABLE);

      assertThat(screenings.getTotalElements()).isEqualTo(1);
      assertThat(screenings.getContent().getFirst().nickname()).isEqualTo("member1");
    }

    @Test
    @DisplayName("시작일과 종료일은 시작일 0시부터 종료일 23시 59분 59초까지 가입한 회원 번호로 바꿔 조회한다.")
    void findScreeningsByStartDateAndEndDate() {
      var condition =
          new ScreeningSearchCondition(
              null, null, null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
      given(
              searchMemberIdsUseCase.searchMemberIds(
                  new MemberIdSearchCondition(
                      null,
                      null,
                      LocalDateTime.of(2026, 1, 1, 0, 0, 0),
                      LocalDateTime.of(2026, 1, 31, 23, 59, 59))))
          .willReturn(Set.of(101L));
      givenScreeningOfMember1(null, Set.of(101L));

      Page<ScreeningView> screenings = screeningQueryService.findScreenings(condition, PAGEABLE);

      assertThat(screenings.getTotalElements()).isEqualTo(1);
      assertThat(screenings.getContent().getFirst().nickname()).isEqualTo("member1");
    }

    @Test
    @DisplayName("시작일만 주면 종료 시각 없이 회원을 찾는다.")
    void findScreeningsByStartDateOnly() {
      var condition =
          new ScreeningSearchCondition(null, null, null, LocalDate.of(2026, 1, 1), null);
      given(
              searchMemberIdsUseCase.searchMemberIds(
                  new MemberIdSearchCondition(
                      null, null, LocalDateTime.of(2026, 1, 1, 0, 0, 0), null)))
          .willReturn(Set.of(101L));
      givenScreeningOfMember1(null, Set.of(101L));

      Page<ScreeningView> screenings = screeningQueryService.findScreenings(condition, PAGEABLE);

      assertThat(screenings.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("모든 조건을 설정해 심사를 조회합니다.")
    void findScreeningsWithAllConditions() {
      var condition =
          new ScreeningSearchCondition(
              "PENDING",
              "member1",
              "01011111111",
              LocalDate.of(2026, 1, 1),
              LocalDate.of(2026, 1, 31));
      given(
              searchMemberIdsUseCase.searchMemberIds(
                  new MemberIdSearchCondition(
                      "member1",
                      "01011111111",
                      LocalDateTime.of(2026, 1, 1, 0, 0, 0),
                      LocalDateTime.of(2026, 1, 31, 23, 59, 59))))
          .willReturn(Set.of(101L));
      givenScreeningOfMember1("PENDING", Set.of(101L));

      Page<ScreeningView> screenings = screeningQueryService.findScreenings(condition, PAGEABLE);

      assertThat(screenings.getContent())
          .containsExactly(new ScreeningView(1L, "member1", "MALE", JOINED_DATE, "PENDING", null));
    }

    @Test
    @DisplayName("빈 문자열 닉네임·전화번호는 조건으로 쓰지 않는다.")
    void ignoreBlankNicknameAndPhoneNumber() {
      var condition = new ScreeningSearchCondition(null, " ", "", null, null);
      givenScreeningOfMember1(null, null);

      Page<ScreeningView> screenings = screeningQueryService.findScreenings(condition, PAGEABLE);

      assertThat(screenings.getTotalElements()).isEqualTo(1);
      verifyNoInteractions(searchMemberIdsUseCase);
    }

    @Test
    @DisplayName("조건에 맞는 회원이 없으면 심사를 조회하지 않고 빈 페이지를 돌려준다.")
    void returnEmptyWhenNoMemberMatches() {
      var condition = new ScreeningSearchCondition("PENDING", "member1", null, null, null);
      given(
              searchMemberIdsUseCase.searchMemberIds(
                  new MemberIdSearchCondition("member1", null, null, null)))
          .willReturn(Set.of());

      Page<ScreeningView> screenings = screeningQueryService.findScreenings(condition, PAGEABLE);

      assertThat(screenings.getTotalElements()).isZero();
      assertThat(screenings.getContent()).isEmpty();
      assertThat(screenings.getPageable()).isEqualTo(PAGEABLE);
      verifyNoInteractions(screeningQueryRepository, findMemberScreeningProfilesUseCase);
    }

    @Test
    @DisplayName("회원 정보를 찾지 못한 심사는 회원 값을 비워 두고 남긴다.")
    void keepScreeningWhenMemberIsMissing() {
      var condition = new ScreeningSearchCondition(null, null, null, null, null);
      given(screeningQueryRepository.findScreenings(null, null, PAGEABLE))
          .willReturn(
              new PageImpl<>(
                  List.of(new RawScreeningView(1L, 101L, "PENDING", null)), PAGEABLE, 1));
      given(findMemberScreeningProfilesUseCase.findMemberScreeningProfiles(Set.of(101L)))
          .willReturn(Map.of());

      Page<ScreeningView> screenings = screeningQueryService.findScreenings(condition, PAGEABLE);

      assertThat(screenings.getContent())
          .containsExactly(new ScreeningView(1L, null, null, null, "PENDING", null));
    }

    private void givenScreeningOfMember1(String screeningStatus, Set<Long> memberIds) {
      given(screeningQueryRepository.findScreenings(screeningStatus, memberIds, PAGEABLE))
          .willReturn(
              new PageImpl<>(
                  List.of(new RawScreeningView(1L, 101L, "PENDING", null)), PAGEABLE, 1));
      given(findMemberScreeningProfilesUseCase.findMemberScreeningProfiles(Set.of(101L)))
          .willReturn(Map.of(101L, member(101L, "member1", "MALE")));
    }
  }

  @Nested
  @DisplayName("심사 상세 조회")
  class GetScreeningDetail {

    @Test
    @DisplayName("상세 심사를 조회합니다.")
    void getScreeningDetail() {
      given(screeningDetailQueryRepository.findById(1L))
          .willReturn(Optional.of(new RawScreeningDetailView(1L, 3L, 101L, "PENDING", null)));
      given(findMemberScreeningProfilesUseCase.findMemberScreeningProfiles(Set.of(101L)))
          .willReturn(Map.of(101L, member(101L, "member", "MALE")));
      given(findMemberScreeningProfilesUseCase.findProfileImages(101L))
          .willReturn(
              List.of(
                  new MemberProfileImage("image_url_1", 1, true),
                  new MemberProfileImage("image_url_2", 2, false)));
      given(findMemberInterviewAnswersUseCase.findPublicInterviewAnswers(101L))
          .willReturn(
              List.of(
                  new MemberInterviewAnswer(
                      11L, "question1", "PERSONAL", 21L, "answer for question1")));

      ScreeningDetailView screeningDetail = screeningQueryService.getScreeningDetail(1L);

      assertThat(screeningDetail)
          .isEqualTo(
              new ScreeningDetailView(
                  1L,
                  3L,
                  new ScreeningDetailProfileView(
                      101L, "PENDING", null, "member", 1998, "MALE", JOINED_DATE),
                  List.of(
                      new ScreeningDetailProfileImageView("image_url_1", 1, true),
                      new ScreeningDetailProfileImageView("image_url_2", 2, false)),
                  List.of(new ScreeningDetailInterviewView("question1", "answer for question1"))));
    }

    @Test
    @DisplayName("반려된 심사는 반려 사유를 담고, 사진과 답변이 없으면 빈 목록을 돌려준다.")
    void getRejectedScreeningDetail() {
      given(screeningDetailQueryRepository.findById(1L))
          .willReturn(
              Optional.of(new RawScreeningDetailView(1L, null, 101L, "REJECTED", "STOLEN_IMAGE")));
      given(findMemberScreeningProfilesUseCase.findMemberScreeningProfiles(Set.of(101L)))
          .willReturn(Map.of(101L, member(101L, "member", "MALE")));
      given(findMemberScreeningProfilesUseCase.findProfileImages(101L)).willReturn(List.of());
      given(findMemberInterviewAnswersUseCase.findPublicInterviewAnswers(101L))
          .willReturn(List.of());

      ScreeningDetailView screeningDetail = screeningQueryService.getScreeningDetail(1L);

      assertThat(screeningDetail.version()).isEqualTo(-1L);
      assertThat(screeningDetail.profile().screeningStatus()).isEqualTo("REJECTED");
      assertThat(screeningDetail.profile().rejectionReason()).isEqualTo("STOLEN_IMAGE");
      assertThat(screeningDetail.profileImages()).isEmpty();
      assertThat(screeningDetail.interviews()).isEmpty();
    }

    @Test
    @DisplayName("없는 심사를 조회하면 예외를 던진다.")
    void throwWhenScreeningNotFound() {
      given(screeningDetailQueryRepository.findById(1L)).willReturn(Optional.empty());

      assertThatThrownBy(() -> screeningQueryService.getScreeningDetail(1L))
          .isInstanceOfSatisfying(
              DeeppleException.class,
              e -> assertThat(e.getErrorCode()).isEqualTo(AdminErrorCode.SCREENING_NOT_FOUND));
      verifyNoInteractions(findMemberScreeningProfilesUseCase, findMemberInterviewAnswersUseCase);
    }

    @Test
    @DisplayName("심사 대상 회원을 찾지 못하면 예외를 던진다.")
    void throwWhenMemberNotFound() {
      given(screeningDetailQueryRepository.findById(1L))
          .willReturn(Optional.of(new RawScreeningDetailView(1L, 0L, 101L, "PENDING", null)));
      given(findMemberScreeningProfilesUseCase.findMemberScreeningProfiles(Set.of(101L)))
          .willReturn(Map.of());

      assertThatThrownBy(() -> screeningQueryService.getScreeningDetail(1L))
          .isInstanceOfSatisfying(
              DeeppleException.class,
              e -> assertThat(e.getErrorCode()).isEqualTo(AdminErrorCode.SCREENING_NOT_FOUND));
      verifyNoInteractions(findMemberInterviewAnswersUseCase);
    }
  }

  private MemberScreeningProfile member(long memberId, String nickname, String gender) {
    return new MemberScreeningProfile(memberId, nickname, 1998, gender, JOINED_AT);
  }
}
