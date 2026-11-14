package kr.co.deepple.recommendation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.application.port.in.ProfileOptions;
import kr.co.deepple.member.application.port.in.ValidateProfileOptionsUseCase;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.recommendation.application.dto.MemberIdealUpdateRequest;
import kr.co.deepple.recommendation.application.dto.MemberIdealView;
import kr.co.deepple.recommendation.application.port.out.MemberIdealQueryRepository;
import kr.co.deepple.recommendation.application.port.out.MemberIdealRepository;
import kr.co.deepple.recommendation.domain.AgeRange;
import kr.co.deepple.recommendation.domain.MemberIdeal;
import kr.co.deepple.recommendation.domain.RecommendationErrorCode;
import kr.co.deepple.recommendation.fixture.MemberIdealFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberIdealServiceTest {

  @InjectMocks private MemberIdealService memberIdealService;

  @Mock private MemberIdealRepository memberIdealRepository;

  @Mock private MemberIdealQueryRepository memberIdealQueryRepository;

  @Mock private ValidateProfileOptionsUseCase validateProfileOptionsUseCase;

  @Nested
  @DisplayName("getMemberIdeal 메서드 테스트")
  class GetMemberIdealTest {

    @Test
    @DisplayName("memberIdeal이 존재하지 않는 경우 예외를 던진다.")
    void throwExceptionWhenMemberIdealIsNotExists() {
      long memberId = 1L;
      when(memberIdealQueryRepository.findMemberIdealByMemberId(memberId))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> memberIdealService.getMemberIdeal(memberId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", RecommendationErrorCode.MEMBER_IDEAL_NOT_FOUND);
    }

    @Test
    @DisplayName("memberIdeal이 존재하는 경우 조회 결과를 돌려준다.")
    void returnMemberIdealView() {
      long memberId = 1L;
      MemberIdealView view =
          new MemberIdealView(20, 30, Set.of("WINE"), Set.of("SEOUL"), "NONE", "NONE", "SOCIAL");
      when(memberIdealQueryRepository.findMemberIdealByMemberId(memberId))
          .thenReturn(Optional.of(view));

      assertThat(memberIdealService.getMemberIdeal(memberId)).isEqualTo(view);
    }
  }

  @Nested
  @DisplayName("update 메서드 테스트")
  class UpdateTest {

    private final AgeRange ageRange = AgeRange.of(20, 30);
    private final MemberIdealUpdateRequest request =
        new MemberIdealUpdateRequest(
            ageRange.getMinAge(),
            ageRange.getMaxAge(),
            Set.of("seoul"),
            "christian",
            "vape",
            "social",
            Set.of("dance", "wine"));
    private final ProfileOptions requestedOptions =
        new ProfileOptions(Set.of("seoul"), Set.of("dance", "wine"), "christian", "vape", "social");

    @Test
    @DisplayName("memberIdeal이 존재하지 않는 경우 예외를 던진다.")
    void throwExceptionWhenMemberIdealIsNotExists() {
      // given
      long memberId = 1L;
      when(memberIdealRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

      // when && then
      assertThatThrownBy(() -> memberIdealService.update(request, memberId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", RecommendationErrorCode.MEMBER_IDEAL_NOT_FOUND);
    }

    @Test
    @DisplayName("프로필 선택 값이 유효하지 않으면 memberIdeal을 바꾸지 않고 예외를 던진다.")
    void throwExceptionWhenProfileOptionIsInvalid() {
      long memberId = 1L;
      MemberIdeal memberIdeal = MemberIdealFixture.builder().memberId(memberId).build();
      when(memberIdealRepository.findByMemberId(memberId)).thenReturn(Optional.of(memberIdeal));
      when(validateProfileOptionsUseCase.validate(requestedOptions))
          .thenThrow(new DeeppleException(MemberErrorCode.INVALID_PROFILE_OPTION, "City=seoul"));

      assertThatThrownBy(() -> memberIdealService.update(request, memberId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.INVALID_PROFILE_OPTION);
      assertThat(memberIdeal.getCities()).isEmpty();
      assertThat(memberIdeal.getAgeRange()).isEqualTo(AgeRange.init());
    }

    @Test
    @DisplayName("memberIdeal이 존재하는 경우 검증을 거쳐 정규화된 값으로 memberIdeal을 업데이트한다.")
    void updateMemberIdeal() {
      long memberId = 1L;
      MemberIdeal memberIdeal = MemberIdealFixture.builder().memberId(memberId).build();
      when(memberIdealRepository.findByMemberId(memberId)).thenReturn(Optional.of(memberIdeal));
      when(validateProfileOptionsUseCase.validate(requestedOptions))
          .thenReturn(
              new ProfileOptions(
                  Set.of("SEOUL"), Set.of("DANCE", "WINE"), "CHRISTIAN", "VAPE", "SOCIAL"));

      // when
      memberIdealService.update(request, memberId);

      // then
      assertThat(memberIdeal.getAgeRange()).isEqualTo(ageRange);
      assertThat(memberIdeal.getHobbies()).containsExactlyInAnyOrder("DANCE", "WINE");
      assertThat(memberIdeal.getCities()).containsExactly("SEOUL");
      assertThat(memberIdeal.getReligion()).isEqualTo("CHRISTIAN");
      assertThat(memberIdeal.getSmokingStatus()).isEqualTo("VAPE");
      assertThat(memberIdeal.getDrinkingStatus()).isEqualTo("SOCIAL");
    }
  }

  @Nested
  @DisplayName("initIfAbsent 메서드 테스트")
  class InitIfAbsentTest {

    @Test
    @DisplayName("memberIdeal이 이미 존재하는 경우 저장하지 않는다.")
    void skipWhenMemberIdealAlreadyExists() {
      // given
      long memberId = 1L;
      when(memberIdealRepository.existsByMemberId(memberId)).thenReturn(true);

      // when
      memberIdealService.initIfAbsent(memberId);

      // then
      verify(memberIdealRepository, never()).save(any());
    }

    @Test
    @DisplayName("memberIdeal이 존재하지 않는 경우 memberIdeal을 생성한다.")
    void createMemberIdeal() {
      // given
      long memberId = 1L;
      when(memberIdealRepository.existsByMemberId(memberId)).thenReturn(false);

      try (MockedStatic<MemberIdeal> memberIdealMock = mockStatic(MemberIdeal.class)) {
        MemberIdeal memberIdeal = mock(MemberIdeal.class);
        memberIdealMock.when(() -> MemberIdeal.init(memberId)).thenReturn(memberIdeal);

        // when
        memberIdealService.initIfAbsent(memberId);

        // then
        verify(memberIdealRepository).save(memberIdeal);
      }
    }
  }
}
